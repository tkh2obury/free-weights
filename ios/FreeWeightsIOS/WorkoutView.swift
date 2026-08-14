import SwiftUI

struct WorkoutView: View {
    @EnvironmentObject private var store: AppStore

    var body: some View {
        ScrollView {
            VStack(spacing: 14) {
                SectionTitle(text: store.state.activeWorkout == nil ? "Start workout" : "Active workout")
                if let active = store.state.activeWorkout, let exercise = store.currentExercise(active: active) {
                    ActiveExerciseView(active: active, exercise: exercise)
                } else {
                    StartWorkoutView()
                }
            }
            .padding()
        }
        .navigationTitle("FREE_WEIGHTS://IOS")
        .navigationBarTitleDisplayMode(.inline)
    }
}

private struct StartWorkoutView: View {
    @EnvironmentObject private var store: AppStore
    @State private var expanded = Set<String>()

    var body: some View {
        if store.state.plans.isEmpty {
            TerminalPanel {
                Label("Create a plan in the Plans tab to begin.", systemImage: "folder.badge.plus")
            }
        } else {
            ForEach(store.state.plans) { plan in
                DisclosureGroup(isExpanded: Binding(get: { expanded.contains(plan.id) }, set: { value in
                    if value { expanded.insert(plan.id) } else { expanded.remove(plan.id) }
                })) {
                    VStack(spacing: 8) {
                        ForEach(plan.days) { day in
                            Button {
                                store.state.selectedPlanId = plan.id
                                store.startWorkout(planId: plan.id, dayId: day.id)
                            } label: {
                                HStack {
                                    Text(day.name)
                                    Spacer()
                                    Text("\(day.exercises.count) EX")
                                    Image(systemName: "play.fill")
                                }
                                .frame(maxWidth: .infinity)
                            }
                            .buttonStyle(.borderedProminent)
                            .disabled(day.exercises.isEmpty)
                        }
                    }
                    .padding(.top, 10)
                } label: {
                    HStack {
                        Image(systemName: "folder.fill")
                        Text(plan.name).font(.headline)
                        Spacer()
                        if nextWorkoutDayId(plan: plan, logs: store.state.logs) != nil { Text("NEXT READY").font(.caption) }
                    }
                }
                .padding(14)
                .overlay(RoundedRectangle(cornerRadius: 10).stroke(TerminalTheme(state: store.state).accent.opacity(0.35)))
            }
        }
    }
}

private struct ActiveExerciseView: View {
    @EnvironmentObject private var store: AppStore
    let active: ActiveWorkout
    let exercise: ExercisePlan
    @State private var remaining = 0
    @State private var phase = "RUN"
    @State private var timerRunning = false
    private let timer = Timer.publish(every: 1, on: .main, in: .common).autoconnect()

    var body: some View {
        TerminalPanel {
            VStack(alignment: .leading, spacing: 12) {
                Text(exercise.name).font(.title2.bold())
                Text("EXERCISE \(active.currentExerciseIndex + 1)").font(.caption).opacity(0.65)
                if exercise.type == .strength { strengthContent } else { runContent }
                Divider()
                HStack {
                    Button("CANCEL", role: .destructive) { store.state.activeWorkout = nil }
                    Spacer()
                    Button("FINISH EXERCISE") { store.saveCurrentExercise() }
                        .buttonStyle(.borderedProminent)
                }
            }
        }
        .onAppear { resetTimer() }
        .onChange(of: exercise.id) { _, _ in resetTimer() }
        .onReceive(timer) { _ in tick() }
    }

    @ViewBuilder
    private var strengthContent: some View {
        let sequence = store.liftSets(exercise)
        let index = active.setResults.count
        if sequence.indices.contains(index) {
            let set = sequence[index]
            Text(set.isWarmup ? "WARM-UP \(set.groupIndex + 1)/\(set.groupCount)" : "WORK SET \(set.groupIndex + 1)/\(set.groupCount)")
                .font(.headline)
            Text("\(numberText(set.weight)) \(store.state.unit.rawValue) × \(set.reps)")
                .font(.system(size: 34, weight: .black, design: .monospaced))
                .lineLimit(1).minimumScaleFactor(0.7)
            PlateStrip(load: calculatePlates(targetWeight: set.weight, barWeight: store.state.barWeight, availablePlates: store.state.availablePlates), unit: store.state.unit)
            HStack {
                Button("MISS") { store.recordSet(succeeded: false, prescription: set) }.buttonStyle(.bordered)
                Button("COMPLETE") { store.recordSet(succeeded: true, prescription: set) }.buttonStyle(.borderedProminent)
            }
        } else {
            Text("All prescribed sets complete.").font(.headline)
        }
        if !active.setResults.isEmpty { Button("UNDO LAST SET") { store.undoSet() }.font(.caption.bold()) }
    }

    @ViewBuilder
    private var runContent: some View {
        let completed = active.setResults.count
        Text("ROUND \(min(completed + 1, exercise.intervalRounds))/\(exercise.intervalRounds)").font(.headline)
        Text(completed >= exercise.intervalRounds ? "COMPLETE" : phase)
            .font(.caption.bold()).opacity(0.7)
        Text(durationText(remaining))
            .font(.system(size: 54, weight: .black, design: .monospaced))
        if completed < exercise.intervalRounds {
            Button(timerRunning ? "PAUSE" : "START") { timerRunning.toggle() }
                .buttonStyle(.borderedProminent)
            Button("SKIP INTERVAL") { finishPhase() }.font(.caption.bold())
        } else {
            Text("All intervals complete.")
        }
    }

    private func resetTimer() {
        phase = "RUN"
        remaining = exercise.runSeconds
        timerRunning = false
    }

    private func tick() {
        guard timerRunning, remaining > 0 else { return }
        remaining -= 1
        if remaining == 0 { finishPhase() }
    }

    private func finishPhase() {
        if phase == "RUN", exercise.walkSeconds > 0 {
            phase = "WALK"
            remaining = exercise.walkSeconds
        } else {
            store.completeRunInterval()
            phase = "RUN"
            remaining = exercise.runSeconds
            if (store.state.activeWorkout?.setResults.count ?? 0) >= exercise.intervalRounds { timerRunning = false }
        }
    }
}

struct PlateStrip: View {
    let load: PlateLoad
    let unit: WeightUnit

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 5) {
                Text("BAR")
                ForEach(Array(load.platesPerSide.enumerated()), id: \.offset) { _, plate in
                    Text(numberText(plate))
                        .font(.caption.bold())
                        .foregroundStyle(plateTextColor(plate))
                        .padding(.horizontal, 6).padding(.vertical, 14)
                        .background(plateColor(plate, unit: unit))
                        .clipShape(RoundedRectangle(cornerRadius: 4))
                }
            }
        }
    }
}

func plateColor(_ value: Double, unit: WeightUnit) -> Color {
    let pounds = unit == .lb ? value : value * 2.20462
    switch pounds {
    case 50...: return .red
    case 40..<50: return .blue
    case 30..<40: return .yellow
    case 20..<30: return .green
    case 12..<20: return .black
    default: return .gray
    }
}

func plateTextColor(_ value: Double) -> Color { value >= 30 && value < 40 ? .black : .white }
