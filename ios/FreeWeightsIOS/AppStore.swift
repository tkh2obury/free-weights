import Foundation

@MainActor
final class AppStore: ObservableObject {
    @Published var state: AppState {
        didSet { save() }
    }

    private let defaults: UserDefaults
    private let key = "free_weights_ios_state_v1"

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
        if let data = defaults.data(forKey: key), let decoded = try? JSONDecoder().decode(AppState.self, from: data) {
            state = decoded
        } else {
            state = AppState()
        }
    }

    private func save() {
        guard let data = try? JSONEncoder().encode(state) else { return }
        defaults.set(data, forKey: key)
    }

    func addPlan(name: String) {
        let plan = WorkoutPlan(name: name)
        state.plans.append(plan)
        state.selectedPlanId = plan.id
    }

    func renamePlan(id: String, name: String) {
        guard let index = state.plans.firstIndex(where: { $0.id == id }) else { return }
        state.plans[index].name = name
    }

    func deletePlan(id: String) {
        state.plans.removeAll { $0.id == id }
        if state.activeWorkout?.planId == id { state.activeWorkout = nil }
        state.selectedPlanId = state.plans.first?.id
    }

    func addDay(planId: String, name: String) {
        guard let index = state.plans.firstIndex(where: { $0.id == planId }) else { return }
        state.plans[index].days.append(WorkoutDay(name: name))
    }

    func deleteDay(planId: String, dayId: String) {
        guard let index = state.plans.firstIndex(where: { $0.id == planId }) else { return }
        state.plans[index].days.removeAll { $0.id == dayId }
        if state.activeWorkout?.dayId == dayId { state.activeWorkout = nil }
    }

    func saveExercise(planId: String, dayId: String, exercise: ExercisePlan) {
        guard let planIndex = state.plans.firstIndex(where: { $0.id == planId }),
              let dayIndex = state.plans[planIndex].days.firstIndex(where: { $0.id == dayId }) else { return }
        if let index = state.plans[planIndex].days[dayIndex].exercises.firstIndex(where: { $0.id == exercise.id }) {
            state.plans[planIndex].days[dayIndex].exercises[index] = exercise
        } else {
            state.plans[planIndex].days[dayIndex].exercises.append(exercise)
        }
        let definition = exercise.definition
        if let index = state.exerciseLibrary.firstIndex(where: { $0.id == definition.id }) {
            state.exerciseLibrary[index] = definition
        } else {
            state.exerciseLibrary.append(definition)
        }
    }

    func deleteExercise(planId: String, dayId: String, exerciseId: String) {
        guard let planIndex = state.plans.firstIndex(where: { $0.id == planId }),
              let dayIndex = state.plans[planIndex].days.firstIndex(where: { $0.id == dayId }) else { return }
        state.plans[planIndex].days[dayIndex].exercises.removeAll { $0.id == exerciseId }
    }

    func deleteLibraryExercise(id: String) {
        state.exerciseLibrary.removeAll { $0.id == id }
        for planIndex in state.plans.indices {
            for dayIndex in state.plans[planIndex].days.indices {
                state.plans[planIndex].days[dayIndex].exercises.removeAll { $0.trackingId == id }
            }
        }
        if let active = state.activeWorkout,
           let exercise = currentExercise(active: active), exercise.trackingId == id {
            state.activeWorkout = nil
        }
    }

    func renameLibraryExercise(id: String, name: String) {
        guard !name.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return }
        let clean = name.trimmingCharacters(in: .whitespacesAndNewlines)
        if let index = state.exerciseLibrary.firstIndex(where: { $0.id == id }) {
            state.exerciseLibrary[index].name = clean
        }
        for planIndex in state.plans.indices {
            for dayIndex in state.plans[planIndex].days.indices {
                for exerciseIndex in state.plans[planIndex].days[dayIndex].exercises.indices
                    where state.plans[planIndex].days[dayIndex].exercises[exerciseIndex].trackingId == id {
                    state.plans[planIndex].days[dayIndex].exercises[exerciseIndex].name = clean
                }
            }
        }
        for index in state.logs.indices where state.logs[index].exerciseId == id {
            state.logs[index].exerciseName = clean
        }
    }

    func deleteAllProgress() {
        state.logs = []
        state.activeWorkout = nil
    }

    func deleteAllPlans() {
        state.plans = []
        state.selectedPlanId = nil
        state.activeWorkout = nil
    }

    func deleteAllExercises() {
        state.exerciseLibrary = []
        for planIndex in state.plans.indices {
            for dayIndex in state.plans[planIndex].days.indices {
                state.plans[planIndex].days[dayIndex].exercises = []
            }
        }
        state.activeWorkout = nil
    }

    func importState(from data: Data) throws {
        state = try JSONDecoder().decode(AppState.self, from: data)
    }

    func exportData() -> Data {
        (try? JSONEncoder().encode(state)) ?? Data()
    }

    func startWorkout(planId: String, dayId: String) {
        guard let plan = state.plans.first(where: { $0.id == planId }),
              let day = plan.days.first(where: { $0.id == dayId }),
              let exercise = day.exercises.first else { return }
        let weight = exercise.type == .strength ? firstSetWeight(exercise) : 0
        state.activeWorkout = ActiveWorkout(planId: planId, dayId: dayId, currentWeight: weight)
    }

    func recordSet(succeeded: Bool, prescription: PrescribedLiftSet) {
        guard var active = state.activeWorkout, let exercise = currentExercise(active: active) else { return }
        active.setResults.append(
            WorkoutSetResult(
                exerciseTrackingId: exercise.trackingId,
                setNumber: active.setResults.count + 1,
                reps: prescription.reps,
                weight: prescription.weight,
                succeeded: succeeded,
                isWarmup: prescription.isWarmup
            )
        )
        let sequence = liftSets(exercise)
        active.currentWeight = sequence.indices.contains(active.setResults.count) ? sequence[active.setResults.count].weight : prescription.weight
        state.activeWorkout = active
    }

    func undoSet() {
        guard var active = state.activeWorkout, let exercise = currentExercise(active: active), !active.setResults.isEmpty else { return }
        active.setResults.removeLast()
        let sequence = liftSets(exercise)
        active.currentWeight = sequence.indices.contains(active.setResults.count) ? sequence[active.setResults.count].weight : firstSetWeight(exercise)
        state.activeWorkout = active
    }

    func completeRunInterval() {
        guard var active = state.activeWorkout, let exercise = currentExercise(active: active) else { return }
        active.setResults.append(
            WorkoutSetResult(
                exerciseTrackingId: exercise.trackingId,
                setNumber: active.setResults.count + 1,
                reps: 1,
                weight: 0,
                succeeded: true
            )
        )
        state.activeWorkout = active
    }

    func saveCurrentExercise() {
        guard let active = state.activeWorkout,
              let plan = state.plans.first(where: { $0.id == active.planId }),
              let day = plan.days.first(where: { $0.id == active.dayId }),
              day.exercises.indices.contains(active.currentExerciseIndex) else { return }
        let exercise = day.exercises[active.currentExerciseIndex]
        let work = active.setResults.filter { !$0.isWarmup }
        let log = WorkoutLog(
            exerciseId: exercise.trackingId,
            exerciseName: exercise.name,
            completedAt: active.startedAt,
            sets: work.count,
            reps: exercise.type == .runWalk ? 1 : work.map(\.reps).max() ?? exercise.targetReps,
            weight: exercise.type == .runWalk ? 0 : work.map(\.weight).max() ?? exercise.workingWeight,
            sessionId: active.sessionId,
            planId: plan.id,
            planName: plan.name,
            dayId: day.id,
            dayName: day.name,
            failedSets: work.filter { !$0.succeeded }.count,
            exerciseType: exercise.type,
            runSeconds: exercise.runSeconds,
            walkSeconds: exercise.walkSeconds,
            intervalRounds: exercise.intervalRounds,
            setResults: exercise.type == .strength ? active.setResults : []
        )
        state.logs.append(log)
        if active.currentExerciseIndex == day.exercises.count - 1 {
            state.activeWorkout = nil
        } else {
            var next = active
            next.currentExerciseIndex += 1
            next.setResults = []
            next.intervalPhase = nil
            let nextExercise = day.exercises[next.currentExerciseIndex]
            next.currentWeight = nextExercise.type == .strength ? firstSetWeight(nextExercise) : 0
            state.activeWorkout = next
        }
    }

    func deleteSession(id: String) {
        state.logs.removeAll { $0.sessionId == id }
    }

    func currentExercise(active: ActiveWorkout? = nil) -> ExercisePlan? {
        guard let active = active ?? state.activeWorkout,
              let plan = state.plans.first(where: { $0.id == active.planId }),
              let day = plan.days.first(where: { $0.id == active.dayId }),
              day.exercises.indices.contains(active.currentExerciseIndex) else { return nil }
        return day.exercises[active.currentExerciseIndex]
    }

    func liftSets(_ exercise: ExercisePlan) -> [PrescribedLiftSet] {
        prescribedLiftSets(
            exercise: exercise,
            workingWeight: suggestedWeight(exercise: exercise, logs: state.logs),
            barWeight: state.barWeight,
            availablePlates: state.availablePlates
        )
    }

    func firstSetWeight(_ exercise: ExercisePlan) -> Double {
        liftSets(exercise).first?.weight ?? state.barWeight
    }
}
