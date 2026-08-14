import SwiftUI

struct PlansView: View {
    @EnvironmentObject private var store: AppStore
    @State private var expandedPlans = Set<String>()
    @State private var expandedDays = Set<String>()
    @State private var prompt: NamePrompt?
    @State private var editor: ExerciseEditorRoute?

    var body: some View {
        ScrollView {
            VStack(spacing: 14) {
                SectionTitle(text: "Plan library")
                Button { prompt = NamePrompt(kind: .plan) } label: {
                    Label("NEW PLAN", systemImage: "plus")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)

                if store.state.plans.isEmpty {
                    TerminalPanel {
                        Text("No plans yet. Create a plan, add a day, then add exercises.")
                    }
                }

                ForEach(store.state.plans) { plan in
                    DisclosureGroup(isExpanded: expansionBinding(plan.id, in: $expandedPlans)) {
                        VStack(spacing: 12) {
                            HStack {
                                Button("RENAME") { prompt = NamePrompt(kind: .renamePlan(plan.id), initial: plan.name) }
                                Spacer()
                                Button("DELETE", role: .destructive) { store.deletePlan(id: plan.id) }
                            }
                            .font(.caption.weight(.bold))

                            ForEach(plan.days) { day in
                                daySection(plan: plan, day: day)
                            }

                            Button { prompt = NamePrompt(kind: .day(plan.id)) } label: {
                                Label("ADD DAY", systemImage: "plus")
                                    .frame(maxWidth: .infinity)
                            }
                            .buttonStyle(.bordered)
                        }
                        .padding(.top, 12)
                    } label: {
                        HStack {
                            Image(systemName: "folder.fill")
                            Text(plan.name).font(.headline)
                            Spacer()
                            Text("\(plan.days.count) DAYS").font(.caption)
                        }
                    }
                    .padding(14)
                    .background(TerminalTheme(state: store.state).panel)
                    .overlay(RoundedRectangle(cornerRadius: 10).stroke(TerminalTheme(state: store.state).accent.opacity(0.35)))
                }
            }
            .padding()
        }
        .navigationTitle("FREE_WEIGHTS://IOS")
        .navigationBarTitleDisplayMode(.inline)
        .alert(prompt?.title ?? "", isPresented: Binding(get: { prompt != nil }, set: { if !$0 { prompt = nil } })) {
            TextField("Name", text: Binding(get: { prompt?.value ?? "" }, set: { prompt?.value = $0 }))
            Button("Cancel", role: .cancel) { prompt = nil }
            Button("Save") { applyPrompt() }
        }
        .sheet(item: $editor) { route in
            ExerciseEditorView(planId: route.planId, dayId: route.dayId, exercise: route.exercise)
                .environmentObject(store)
        }
    }

    @ViewBuilder
    private func daySection(plan: WorkoutPlan, day: WorkoutDay) -> some View {
        DisclosureGroup(isExpanded: expansionBinding(day.id, in: $expandedDays)) {
            VStack(spacing: 10) {
                ForEach(day.exercises) { exercise in
                    HStack(spacing: 10) {
                        Image(systemName: exercise.type == .strength ? "dumbbell.fill" : "figure.run")
                        VStack(alignment: .leading, spacing: 3) {
                            Text(exercise.name).font(.headline)
                            Text(exerciseSummary(exercise)).font(.caption).opacity(0.65).lineLimit(1)
                        }
                        Spacer()
                        Button { editor = ExerciseEditorRoute(planId: plan.id, dayId: day.id, exercise: exercise) } label: {
                            Image(systemName: "pencil")
                        }
                        Button(role: .destructive) { store.deleteExercise(planId: plan.id, dayId: day.id, exerciseId: exercise.id) } label: {
                            Image(systemName: "trash")
                        }
                    }
                    .padding(.vertical, 5)
                }
                Button { editor = ExerciseEditorRoute(planId: plan.id, dayId: day.id) } label: {
                    Label("ADD EXERCISE", systemImage: "plus").frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
                Button("DELETE DAY", role: .destructive) { store.deleteDay(planId: plan.id, dayId: day.id) }
                    .font(.caption.bold())
            }
            .padding(.top, 10)
        } label: {
            HStack {
                Text(day.name).font(.headline)
                Spacer()
                Text("\(day.exercises.count) EXERCISES").font(.caption)
            }
        }
        .padding(12)
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(TerminalTheme(state: store.state).accent.opacity(0.25)))
    }

    private func exerciseSummary(_ exercise: ExercisePlan) -> String {
        if exercise.type == .runWalk {
            return "\(exercise.intervalRounds) × \(durationText(exercise.runSeconds)) run / \(durationText(exercise.walkSeconds)) walk"
        }
        return "\(exercise.targetSets) × \(exercise.targetReps) | \(numberText(exercise.workingWeight)) \(store.state.unit.rawValue)"
    }

    private func expansionBinding(_ id: String, in set: Binding<Set<String>>) -> Binding<Bool> {
        Binding(get: { set.wrappedValue.contains(id) }, set: { expanded in
            if expanded { set.wrappedValue.insert(id) } else { set.wrappedValue.remove(id) }
        })
    }

    private func applyPrompt() {
        guard let prompt, !prompt.value.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return }
        switch prompt.kind {
        case .plan:
            store.addPlan(name: prompt.value)
        case .renamePlan(let id):
            store.renamePlan(id: id, name: prompt.value)
        case .day(let planId):
            store.addDay(planId: planId, name: prompt.value)
        }
        self.prompt = nil
    }
}

private struct ExerciseEditorRoute: Identifiable {
    let id = UUID()
    let planId: String
    let dayId: String
    var exercise: ExercisePlan?
}

private struct NamePrompt {
    enum Kind { case plan, renamePlan(String), day(String) }
    var kind: Kind
    var value: String
    var title: String {
        switch kind {
        case .plan: return "New plan"
        case .renamePlan: return "Rename plan"
        case .day: return "New day"
        }
    }
    init(kind: Kind, initial: String = "") { self.kind = kind; value = initial }
}
