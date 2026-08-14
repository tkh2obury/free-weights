import Foundation

public struct PlateLoad: Equatable, Sendable {
    public var platesPerSide: [Double]
    public var loadedWeight: Double
    public var remainder: Double
    public var isExact: Bool
}

public func calculatePlates(targetWeight: Double, barWeight: Double, availablePlates: [Double]) -> PlateLoad {
    guard targetWeight > barWeight, !availablePlates.isEmpty else {
        return PlateLoad(
            platesPerSide: [],
            loadedWeight: barWeight,
            remainder: targetWeight - barWeight,
            isExact: abs(targetWeight - barWeight) < 0.001
        )
    }
    var perSide = (targetWeight - barWeight) / 2
    var plates: [Double] = []
    for plate in availablePlates.filter({ $0 > 0 }).sorted(by: >) {
        while perSide + 0.0001 >= plate {
            plates.append(plate)
            perSide -= plate
        }
    }
    let loaded = barWeight + plates.reduce(0, +) * 2
    let remainder = targetWeight - loaded
    return PlateLoad(platesPerSide: plates, loadedWeight: loaded, remainder: remainder, isExact: abs(remainder) < 0.001)
}

public struct PrescribedLiftSet: Equatable, Sendable {
    public var reps: Int
    public var weight: Double
    public var isWarmup: Bool
    public var groupIndex: Int
    public var groupCount: Int
}

public func prescribedLiftSets(
    exercise: ExercisePlan,
    workingWeight: Double,
    barWeight: Double,
    availablePlates: [Double]
) -> [PrescribedLiftSet] {
    let warmups = exercise.warmupSets.enumerated().map { index, set in
        let target = set.weightPercent <= 0 ? barWeight : max(barWeight, workingWeight * Double(set.weightPercent) / 100)
        return PrescribedLiftSet(
            reps: set.reps,
            weight: calculatePlates(targetWeight: target, barWeight: barWeight, availablePlates: availablePlates).loadedWeight,
            isWarmup: true,
            groupIndex: index,
            groupCount: exercise.warmupSets.count
        )
    }
    let scheme = exercise.workSets.isEmpty
        ? Array(repeating: WorkSetPlan(reps: exercise.targetReps, weightOffset: 0), count: exercise.targetSets)
        : exercise.workSets
    let work = scheme.enumerated().map { index, set in
        let target = max(barWeight, workingWeight + set.weightOffset)
        return PrescribedLiftSet(
            reps: set.reps,
            weight: calculatePlates(targetWeight: target, barWeight: barWeight, availablePlates: availablePlates).loadedWeight,
            isWarmup: false,
            groupIndex: index,
            groupCount: scheme.count
        )
    }
    return warmups + work
}

public func suggestedWeight(exercise: ExercisePlan, logs: [WorkoutLog]) -> Double {
    guard let latest = logs.filter({ $0.exerciseId == exercise.trackingId }).max(by: { $0.completedAt < $1.completedAt }) else {
        return exercise.workingWeight
    }
    let requiredSets = exercise.workSets.isEmpty ? exercise.targetSets : exercise.workSets.count
    let requiredReps = exercise.workSets.map(\.reps).max() ?? exercise.targetReps
    let completed = latest.sets >= requiredSets && latest.reps >= requiredReps && latest.failedSets == 0
    return completed ? latest.weight + exercise.increment : latest.weight
}

public func nextWorkoutDayId(plan: WorkoutPlan?, logs: [WorkoutLog]) -> String? {
    guard let plan, !plan.days.isEmpty else { return nil }
    guard let last = logs.filter({ $0.planId == plan.id }).max(by: { $0.completedAt < $1.completedAt }),
          let index = plan.days.firstIndex(where: { $0.id == last.dayId }) else {
        return plan.days.first?.id
    }
    return plan.days[(index + 1) % plan.days.count].id
}

public func durationText(_ seconds: Int) -> String {
    String(format: "%d:%02d", max(0, seconds) / 60, max(0, seconds) % 60)
}

public func numberText(_ value: Double) -> String {
    value.rounded() == value ? String(Int(value)) : String(format: "%.2f", value).replacingOccurrences(of: #"0+$"#, with: "", options: .regularExpression).replacingOccurrences(of: #"\.$"#, with: "", options: .regularExpression)
}
