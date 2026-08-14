import SwiftUI

struct ExerciseEditorView: View {
    @EnvironmentObject private var store: AppStore
    @Environment(\.dismiss) private var dismiss
    let planId: String
    let dayId: String
    private let originalId: String

    @State private var trackingId: String
    @State private var name: String
    @State private var type: ExerciseType
    @State private var sets: Int
    @State private var reps: Int
    @State private var weight: Double
    @State private var increment: Double
    @State private var restSeconds: Int
    @State private var runSeconds: Int
    @State private var walkSeconds: Int
    @State private var rounds: Int
    @State private var warmups: [WarmupSetPlan]
    @State private var workSets: [WorkSetPlan]
    @State private var showAdvanced = false

    init(planId: String, dayId: String, exercise: ExercisePlan? = nil) {
        self.planId = planId
        self.dayId = dayId
        let value = exercise ?? ExercisePlan(name: "")
        originalId = value.id
        _trackingId = State(initialValue: value.trackingId)
        _name = State(initialValue: value.name)
        _type = State(initialValue: value.type)
        _sets = State(initialValue: value.targetSets)
        _reps = State(initialValue: value.targetReps)
        _weight = State(initialValue: value.workingWeight)
        _increment = State(initialValue: value.increment)
        _restSeconds = State(initialValue: value.restSeconds)
        _runSeconds = State(initialValue: value.runSeconds)
        _walkSeconds = State(initialValue: value.walkSeconds)
        _rounds = State(initialValue: value.intervalRounds)
        _warmups = State(initialValue: value.warmupSets)
        _workSets = State(initialValue: value.workSets)
    }

    var body: some View {
        NavigationStack {
            Form {
                if !store.state.exerciseLibrary.isEmpty {
                    Section("USE SAVED EXERCISE") {
                        Picker("Saved exercise", selection: $trackingId) {
                            Text("Custom").tag("")
                            ForEach(store.state.exerciseLibrary.sorted { $0.name < $1.name }) { item in
                                Text(item.name).tag(item.id)
                            }
                        }
                        .onChange(of: trackingId) { _, id in loadDefinition(id) }
                    }
                }

                Section("EXERCISE") {
                    TextField("Name", text: $name)
                    Picker("Type", selection: $type) {
                        Text("STRENGTH").tag(ExerciseType.strength)
                        Text("RUN / WALK").tag(ExerciseType.runWalk)
                    }
                    .pickerStyle(.segmented)
                }

                if type == .strength {
                    Section("WORKING SETS") {
                        Stepper("Sets: \(sets)", value: $sets, in: 1...20)
                        Stepper("Reps: \(reps)", value: $reps, in: 1...100)
                        numberField("Working weight", value: $weight)
                        numberField("Progress increment", value: $increment)
                        Stepper("Rest: \(durationText(restSeconds))", value: $restSeconds, in: 0...900, step: 15)
                        Text("Loaded weight includes the \(numberText(store.state.barWeight)) \(store.state.unit.rawValue) bar and uses enabled plate sizes.")
                            .font(.caption).opacity(0.65)
                    }

                    Section {
                        DisclosureGroup("ADVANCED WARM-UPS + PYRAMID", isExpanded: $showAdvanced) {
                            Text("Warm up from an empty bar or very light load, building to about 80% of working weight without fatigue.")
                                .font(.caption).opacity(0.7)
                            ForEach(warmups.indices, id: \.self) { index in
                                HStack {
                                    Stepper("\(warmups[index].reps) reps", value: $warmups[index].reps, in: 1...20)
                                    Stepper("\(warmups[index].weightPercent)%", value: $warmups[index].weightPercent, in: 0...100, step: 5)
                                    Button(role: .destructive) { warmups.remove(at: index) } label: { Image(systemName: "trash") }
                                }
                            }
                            Button("ADD WARM-UP SET") {
                                warmups.append(WarmupSetPlan(reps: warmups.isEmpty ? 10 : 5, weightPercent: min(80, (warmups.last?.weightPercent ?? 0) + 20)))
                            }

                            Divider()
                            Text("Custom work sets use offsets from working weight. Negative and positive offsets create pyramid schemes.")
                                .font(.caption).opacity(0.7)
                            ForEach(workSets.indices, id: \.self) { index in
                                HStack {
                                    Stepper("\(workSets[index].reps) reps", value: $workSets[index].reps, in: 1...100)
                                    numberField("± weight", value: $workSets[index].weightOffset)
                                    Button(role: .destructive) { workSets.remove(at: index) } label: { Image(systemName: "trash") }
                                }
                            }
                            Button("ADD WORK SET") { workSets.append(WorkSetPlan(reps: reps, weightOffset: 0)) }
                            Button("LOAD RECOMMENDED WARM-UP") {
                                warmups = [
                                    WarmupSetPlan(reps: 10, weightPercent: 0),
                                    WarmupSetPlan(reps: 5, weightPercent: 40),
                                    WarmupSetPlan(reps: 3, weightPercent: 60),
                                    WarmupSetPlan(reps: 2, weightPercent: 80)
                                ]
                            }
                        }
                    }
                } else {
                    Section("INTERVALS") {
                        Stepper("Run: \(durationText(runSeconds))", value: $runSeconds, in: 5...3600, step: 5)
                        Stepper("Walk: \(durationText(walkSeconds))", value: $walkSeconds, in: 0...3600, step: 5)
                        Stepper("Rounds: \(rounds)", value: $rounds, in: 1...100)
                        Text("Total session: \(durationText((runSeconds + walkSeconds) * rounds))")
                    }
                }
            }
            .scrollContentBackground(.hidden)
            .terminalScreen(store.state)
            .navigationTitle(name.isEmpty ? "Add exercise" : name)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("Cancel") { dismiss() } }
                ToolbarItem(placement: .confirmationAction) { Button("Save", action: save).disabled(name.trimmingCharacters(in: .whitespaces).isEmpty) }
            }
        }
    }

    @ViewBuilder
    private func numberField(_ label: String, value: Binding<Double>) -> some View {
        TextField(label, value: value, format: .number)
            .keyboardType(.decimalPad)
            .textFieldStyle(.roundedBorder)
    }

    private func loadDefinition(_ id: String) {
        guard let item = store.state.exerciseLibrary.first(where: { $0.id == id }) else { return }
        name = item.name; type = item.type; sets = item.targetSets; reps = item.targetReps
        weight = item.workingWeight; increment = item.increment; restSeconds = item.restSeconds
        runSeconds = item.runSeconds; walkSeconds = item.walkSeconds; rounds = item.intervalRounds
        warmups = item.warmupSets; workSets = item.workSets
    }

    private func save() {
        let exercise = ExercisePlan(
            id: originalId,
            trackingId: trackingId.isEmpty ? nil : trackingId,
            name: name.trimmingCharacters(in: .whitespacesAndNewlines),
            targetSets: sets,
            targetReps: reps,
            workingWeight: weight,
            increment: increment,
            restSeconds: restSeconds,
            type: type,
            runSeconds: runSeconds,
            walkSeconds: walkSeconds,
            intervalRounds: rounds,
            warmupSets: warmups,
            workSets: workSets
        )
        store.saveExercise(planId: planId, dayId: dayId, exercise: exercise)
        dismiss()
    }
}
