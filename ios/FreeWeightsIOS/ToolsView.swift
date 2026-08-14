import SwiftUI
import UniformTypeIdentifiers

struct ToolsView: View {
    @EnvironmentObject private var store: AppStore
    @State private var targetWeight = 135.0
    @State private var rename: RenameExercise?
    @State private var destructiveAction: DestructiveAction?
    @State private var exporting = false
    @State private var importing = false
    @State private var importError: String?

    private var load: PlateLoad {
        calculatePlates(targetWeight: targetWeight, barWeight: store.state.barWeight, availablePlates: store.state.availablePlates)
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 14) {
                SectionTitle(text: "Plate calculator")
                TerminalPanel {
                    VStack(alignment: .leading, spacing: 12) {
                        TextField("Target weight", value: $targetWeight, format: .number)
                            .keyboardType(.decimalPad).textFieldStyle(.roundedBorder)
                        Text("BAR \(numberText(store.state.barWeight)) \(store.state.unit.rawValue) | LOADED \(numberText(load.loadedWeight))")
                            .font(.headline).lineLimit(1).minimumScaleFactor(0.7)
                        PlateStrip(load: load, unit: store.state.unit)
                        if !load.isExact { Text("REMAINDER \(numberText(load.remainder))").font(.caption) }
                    }
                }

                SectionTitle(text: "Equipment")
                TerminalPanel {
                    VStack(alignment: .leading, spacing: 10) {
                        Picker("Unit", selection: $store.state.unit) {
                            Text("LB").tag(WeightUnit.lb)
                            Text("KG").tag(WeightUnit.kg)
                        }.pickerStyle(.segmented)
                        TextField("Bar weight", value: barBinding, format: .number)
                            .keyboardType(.decimalPad).textFieldStyle(.roundedBorder)
                        Text("AVAILABLE PLATES / SIDE").font(.caption.bold())
                        ForEach(allPlateChoices, id: \.self) { plate in
                            Toggle(isOn: plateBinding(plate)) {
                                HStack {
                                    Circle().fill(plateColor(plate, unit: store.state.unit)).frame(width: 18, height: 18)
                                    Text(numberText(plate))
                                }
                            }
                        }
                    }
                }

                SectionTitle(text: "Exercise library")
                TerminalPanel {
                    if store.state.exerciseLibrary.isEmpty {
                        Text("No saved exercises.").opacity(0.65)
                    }
                    ForEach(store.state.exerciseLibrary.sorted { $0.name < $1.name }) { exercise in
                        HStack {
                            Text(exercise.name)
                            Spacer()
                            Button { rename = RenameExercise(id: exercise.id, value: exercise.name) } label: { Image(systemName: "pencil") }
                            Button(role: .destructive) { store.deleteLibraryExercise(id: exercise.id) } label: { Image(systemName: "trash") }
                        }
                        Divider()
                    }
                }

                SectionTitle(text: "Theme")
                TerminalPanel {
                    VStack(alignment: .leading, spacing: 10) {
                        Text("DEFAULT TEXT COLOR #00FF66").font(.caption)
                        TextField("Text hex", text: $store.state.themeTextColor).textFieldStyle(.roundedBorder)
                        TextField("Background hex", text: $store.state.themeBackgroundColor).textFieldStyle(.roundedBorder)
                    }
                }

                SectionTitle(text: "Data management")
                TerminalPanel {
                    VStack(spacing: 10) {
                        Button("EXPORT ALL DATA") { exporting = true }.buttonStyle(.bordered)
                        Button("IMPORT DATA") { importing = true }.buttonStyle(.bordered)
                        Button("DELETE ALL PROGRESS", role: .destructive) { destructiveAction = .progress }
                        Button("DELETE ALL PLANS", role: .destructive) { destructiveAction = .plans }
                        Button("DELETE ALL EXERCISES", role: .destructive) { destructiveAction = .exercises }
                    }
                    .frame(maxWidth: .infinity)
                }
            }
            .padding()
        }
        .navigationTitle("FREE_WEIGHTS://IOS")
        .navigationBarTitleDisplayMode(.inline)
        .alert(rename == nil ? "" : "Rename exercise", isPresented: Binding(get: { rename != nil }, set: { if !$0 { rename = nil } })) {
            TextField("Name", text: Binding(get: { rename?.value ?? "" }, set: { rename?.value = $0 }))
            Button("Cancel", role: .cancel) { rename = nil }
            Button("Save") {
                if let rename { store.renameLibraryExercise(id: rename.id, name: rename.value) }
                rename = nil
            }
        }
        .confirmationDialog(destructiveAction?.title ?? "", isPresented: Binding(get: { destructiveAction != nil }, set: { if !$0 { destructiveAction = nil } }), titleVisibility: .visible) {
            Button("Delete", role: .destructive) { applyDestructiveAction() }
            Button("Cancel", role: .cancel) { destructiveAction = nil }
        }
        .fileExporter(isPresented: $exporting, document: StateDocument(data: store.exportData()), contentType: .json, defaultFilename: "free-weights-backup") { _ in }
        .fileImporter(isPresented: $importing, allowedContentTypes: [.json]) { result in
            do {
                let url = try result.get()
                let granted = url.startAccessingSecurityScopedResource()
                defer { if granted { url.stopAccessingSecurityScopedResource() } }
                try store.importState(from: Data(contentsOf: url))
            } catch { importError = error.localizedDescription }
        }
        .alert("Import failed", isPresented: Binding(get: { importError != nil }, set: { if !$0 { importError = nil } })) {
            Button("OK") { importError = nil }
        } message: { Text(importError ?? "Unknown error") }
    }

    private var barBinding: Binding<Double> {
        Binding(get: { store.state.barWeight }, set: { value in
            if store.state.unit == .lb { store.state.lbBarWeight = value } else { store.state.kgBarWeight = value }
        })
    }

    private var allPlateChoices: [Double] {
        store.state.unit == .lb ? [55, 45, 35, 25, 15, 10, 5, 2.5] : [25, 20, 15, 10, 5, 2.5, 1.25]
    }

    private func plateBinding(_ plate: Double) -> Binding<Bool> {
        Binding(get: { store.state.availablePlates.contains(plate) }, set: { enabled in
            var values = store.state.availablePlates.filter { $0 != plate }
            if enabled { values.append(plate) }
            values.sort(by: >)
            if store.state.unit == .lb { store.state.availableLbPlates = values } else { store.state.availableKgPlates = values }
        })
    }

    private func applyDestructiveAction() {
        switch destructiveAction {
        case .progress: store.deleteAllProgress()
        case .plans: store.deleteAllPlans()
        case .exercises: store.deleteAllExercises()
        case nil: break
        }
        destructiveAction = nil
    }
}

private struct RenameExercise { let id: String; var value: String }

private enum DestructiveAction {
    case progress, plans, exercises
    var title: String {
        switch self {
        case .progress: return "Delete all progress?"
        case .plans: return "Delete all plans?"
        case .exercises: return "Delete all exercises?"
        }
    }
}

private struct StateDocument: FileDocument {
    static var readableContentTypes: [UTType] { [.json] }
    var data: Data
    init(data: Data) { self.data = data }
    init(configuration: ReadConfiguration) throws { data = configuration.file.regularFileContents ?? Data() }
    func fileWrapper(configuration: WriteConfiguration) throws -> FileWrapper { FileWrapper(regularFileWithContents: data) }
}
