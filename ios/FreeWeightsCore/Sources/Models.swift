import Foundation

public enum WeightUnit: String, Codable, CaseIterable, Sendable {
    case lb
    case kg
}

public enum ExerciseType: String, Codable, CaseIterable, Sendable {
    case strength
    case runWalk
}

public struct WarmupSetPlan: Codable, Equatable, Sendable {
    public var reps: Int
    public var weightPercent: Int

    public init(reps: Int, weightPercent: Int) {
        self.reps = reps
        self.weightPercent = weightPercent
    }
}

public struct WorkSetPlan: Codable, Equatable, Sendable {
    public var reps: Int
    public var weightOffset: Double

    public init(reps: Int, weightOffset: Double) {
        self.reps = reps
        self.weightOffset = weightOffset
    }
}

public struct ExerciseDefinition: Identifiable, Codable, Equatable, Sendable {
    public var id: String
    public var name: String
    public var targetSets: Int
    public var targetReps: Int
    public var workingWeight: Double
    public var increment: Double
    public var restSeconds: Int
    public var type: ExerciseType
    public var runSeconds: Int
    public var walkSeconds: Int
    public var intervalRounds: Int
    public var warmupSets: [WarmupSetPlan]
    public var workSets: [WorkSetPlan]

    public init(
        id: String = UUID().uuidString,
        name: String,
        targetSets: Int = 3,
        targetReps: Int = 5,
        workingWeight: Double = 45,
        increment: Double = 5,
        restSeconds: Int = 90,
        type: ExerciseType = .strength,
        runSeconds: Int = 60,
        walkSeconds: Int = 60,
        intervalRounds: Int = 5,
        warmupSets: [WarmupSetPlan] = [],
        workSets: [WorkSetPlan] = []
    ) {
        self.id = id
        self.name = name
        self.targetSets = targetSets
        self.targetReps = targetReps
        self.workingWeight = workingWeight
        self.increment = increment
        self.restSeconds = restSeconds
        self.type = type
        self.runSeconds = runSeconds
        self.walkSeconds = walkSeconds
        self.intervalRounds = intervalRounds
        self.warmupSets = warmupSets
        self.workSets = workSets
    }
}

public struct ExercisePlan: Identifiable, Codable, Equatable, Sendable {
    public var id: String
    public var trackingId: String
    public var name: String
    public var targetSets: Int
    public var targetReps: Int
    public var workingWeight: Double
    public var increment: Double
    public var restSeconds: Int
    public var type: ExerciseType
    public var runSeconds: Int
    public var walkSeconds: Int
    public var intervalRounds: Int
    public var warmupSets: [WarmupSetPlan]
    public var workSets: [WorkSetPlan]

    public init(
        id: String = UUID().uuidString,
        trackingId: String? = nil,
        name: String,
        targetSets: Int = 3,
        targetReps: Int = 5,
        workingWeight: Double = 45,
        increment: Double = 5,
        restSeconds: Int = 90,
        type: ExerciseType = .strength,
        runSeconds: Int = 60,
        walkSeconds: Int = 60,
        intervalRounds: Int = 5,
        warmupSets: [WarmupSetPlan] = [],
        workSets: [WorkSetPlan] = []
    ) {
        self.id = id
        self.trackingId = trackingId ?? id
        self.name = name
        self.targetSets = targetSets
        self.targetReps = targetReps
        self.workingWeight = workingWeight
        self.increment = increment
        self.restSeconds = restSeconds
        self.type = type
        self.runSeconds = runSeconds
        self.walkSeconds = walkSeconds
        self.intervalRounds = intervalRounds
        self.warmupSets = warmupSets
        self.workSets = workSets
    }

    public var definition: ExerciseDefinition {
        ExerciseDefinition(
            id: trackingId,
            name: name,
            targetSets: targetSets,
            targetReps: targetReps,
            workingWeight: workingWeight,
            increment: increment,
            restSeconds: restSeconds,
            type: type,
            runSeconds: runSeconds,
            walkSeconds: walkSeconds,
            intervalRounds: intervalRounds,
            warmupSets: warmupSets,
            workSets: workSets
        )
    }
}

public struct WorkoutDay: Identifiable, Codable, Equatable, Sendable {
    public var id: String
    public var name: String
    public var exercises: [ExercisePlan]

    public init(id: String = UUID().uuidString, name: String, exercises: [ExercisePlan] = []) {
        self.id = id
        self.name = name
        self.exercises = exercises
    }
}

public struct WorkoutPlan: Identifiable, Codable, Equatable, Sendable {
    public var id: String
    public var name: String
    public var days: [WorkoutDay]

    public init(id: String = UUID().uuidString, name: String, days: [WorkoutDay] = []) {
        self.id = id
        self.name = name
        self.days = days
    }
}

public struct WorkoutSetResult: Identifiable, Codable, Equatable, Sendable {
    public var id: String
    public var exerciseTrackingId: String
    public var setNumber: Int
    public var reps: Int
    public var weight: Double
    public var succeeded: Bool
    public var isWarmup: Bool

    public init(
        id: String = UUID().uuidString,
        exerciseTrackingId: String,
        setNumber: Int,
        reps: Int,
        weight: Double,
        succeeded: Bool,
        isWarmup: Bool = false
    ) {
        self.id = id
        self.exerciseTrackingId = exerciseTrackingId
        self.setNumber = setNumber
        self.reps = reps
        self.weight = weight
        self.succeeded = succeeded
        self.isWarmup = isWarmup
    }
}

public struct ActiveWorkout: Codable, Equatable, Sendable {
    public var sessionId: String
    public var startedAt: Date
    public var planId: String
    public var dayId: String
    public var currentExerciseIndex: Int
    public var setResults: [WorkoutSetResult]
    public var currentWeight: Double?
    public var intervalPhase: String?

    public init(
        sessionId: String = UUID().uuidString,
        startedAt: Date = Date(),
        planId: String,
        dayId: String,
        currentExerciseIndex: Int = 0,
        setResults: [WorkoutSetResult] = [],
        currentWeight: Double? = nil,
        intervalPhase: String? = nil
    ) {
        self.sessionId = sessionId
        self.startedAt = startedAt
        self.planId = planId
        self.dayId = dayId
        self.currentExerciseIndex = currentExerciseIndex
        self.setResults = setResults
        self.currentWeight = currentWeight
        self.intervalPhase = intervalPhase
    }
}

public struct WorkoutLog: Identifiable, Codable, Equatable, Sendable {
    public var id: String
    public var exerciseId: String
    public var exerciseName: String
    public var completedAt: Date
    public var sets: Int
    public var reps: Int
    public var weight: Double
    public var sessionId: String
    public var planId: String
    public var planName: String
    public var dayId: String
    public var dayName: String
    public var failedSets: Int
    public var exerciseType: ExerciseType
    public var runSeconds: Int
    public var walkSeconds: Int
    public var intervalRounds: Int
    public var setResults: [WorkoutSetResult]

    public init(
        id: String = UUID().uuidString,
        exerciseId: String,
        exerciseName: String,
        completedAt: Date = Date(),
        sets: Int,
        reps: Int,
        weight: Double,
        sessionId: String? = nil,
        planId: String = "",
        planName: String = "Workout",
        dayId: String = "",
        dayName: String = "Session",
        failedSets: Int = 0,
        exerciseType: ExerciseType = .strength,
        runSeconds: Int = 0,
        walkSeconds: Int = 0,
        intervalRounds: Int = 0,
        setResults: [WorkoutSetResult] = []
    ) {
        self.id = id
        self.exerciseId = exerciseId
        self.exerciseName = exerciseName
        self.completedAt = completedAt
        self.sets = sets
        self.reps = reps
        self.weight = weight
        self.sessionId = sessionId ?? id
        self.planId = planId
        self.planName = planName
        self.dayId = dayId
        self.dayName = dayName
        self.failedSets = failedSets
        self.exerciseType = exerciseType
        self.runSeconds = runSeconds
        self.walkSeconds = walkSeconds
        self.intervalRounds = intervalRounds
        self.setResults = setResults
    }

    public var volume: Double {
        if !setResults.isEmpty {
            return setResults.filter { $0.succeeded && !$0.isWarmup }.reduce(0) { $0 + Double($1.reps) * $1.weight }
        }
        return Double(max(0, sets - failedSets) * reps) * weight
    }
}

public struct AppState: Codable, Equatable, Sendable {
    public var unit: WeightUnit
    public var plans: [WorkoutPlan]
    public var selectedPlanId: String?
    public var exerciseLibrary: [ExerciseDefinition]
    public var activeWorkout: ActiveWorkout?
    public var logs: [WorkoutLog]
    public var availableLbPlates: [Double]
    public var availableKgPlates: [Double]
    public var lbBarWeight: Double
    public var kgBarWeight: Double
    public var themeTextColor: String
    public var themeBackgroundColor: String

    public init(
        unit: WeightUnit = .lb,
        plans: [WorkoutPlan] = [],
        selectedPlanId: String? = nil,
        exerciseLibrary: [ExerciseDefinition] = [],
        activeWorkout: ActiveWorkout? = nil,
        logs: [WorkoutLog] = [],
        availableLbPlates: [Double] = [45, 25, 10, 5, 2.5],
        availableKgPlates: [Double] = [25, 20, 15, 10, 5, 2.5, 1.25],
        lbBarWeight: Double = 45,
        kgBarWeight: Double = 20,
        themeTextColor: String = "#00FF66",
        themeBackgroundColor: String = "#020704"
    ) {
        self.unit = unit
        self.plans = plans
        self.selectedPlanId = selectedPlanId
        self.exerciseLibrary = exerciseLibrary
        self.activeWorkout = activeWorkout
        self.logs = logs
        self.availableLbPlates = availableLbPlates
        self.availableKgPlates = availableKgPlates
        self.lbBarWeight = lbBarWeight
        self.kgBarWeight = kgBarWeight
        self.themeTextColor = themeTextColor
        self.themeBackgroundColor = themeBackgroundColor
    }

    public var selectedPlan: WorkoutPlan? {
        plans.first(where: { $0.id == selectedPlanId }) ?? plans.first
    }

    public var availablePlates: [Double] {
        unit == .lb ? availableLbPlates : availableKgPlates
    }

    public var barWeight: Double {
        unit == .lb ? lbBarWeight : kgBarWeight
    }
}
