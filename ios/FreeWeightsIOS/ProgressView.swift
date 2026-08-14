import Charts
import SwiftUI

struct ProgressView: View {
    @EnvironmentObject private var store: AppStore
    @State private var expandedSessions = Set<String>()

    private var sessions: [(id: String, logs: [WorkoutLog])] {
        Dictionary(grouping: store.state.logs, by: \.sessionId)
            .map { ($0.key, $0.value.sorted { $0.completedAt < $1.completedAt }) }
            .sorted { ($0.logs.first?.completedAt ?? .distantPast) > ($1.logs.first?.completedAt ?? .distantPast) }
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 14) {
                SectionTitle(text: "Progress")
                HStack(spacing: 12) {
                    stat("SESSIONS", "\(sessions.count)")
                    stat("VOLUME", numberText(store.state.logs.reduce(0) { $0 + $1.volume }))
                }

                ForEach(store.state.exerciseLibrary) { exercise in
                    let logs = store.state.logs.filter { $0.exerciseId == exercise.id }
                    if !logs.isEmpty {
                        TerminalPanel {
                            VStack(alignment: .leading, spacing: 10) {
                                Text(exercise.name).font(.headline)
                                if exercise.type == .runWalk {
                                    RunDurationChart(logs: logs)
                                } else {
                                    Chart(logs.sorted { $0.completedAt < $1.completedAt }) { log in
                                        LineMark(x: .value("Date", log.completedAt), y: .value("Weight", log.weight))
                                            .foregroundStyle(TerminalTheme(state: store.state).accent)
                                        PointMark(x: .value("Date", log.completedAt), y: .value("Weight", log.weight))
                                            .foregroundStyle(TerminalTheme(state: store.state).accent)
                                    }
                                    .frame(height: 170)
                                }
                            }
                        }
                    }
                }

                SectionTitle(text: "Recent session folders")
                ForEach(sessions, id: \.id) { session in
                    let first = session.logs.first
                    DisclosureGroup(isExpanded: Binding(get: { expandedSessions.contains(session.id) }, set: { value in
                        if value { expandedSessions.insert(session.id) } else { expandedSessions.remove(session.id) }
                    })) {
                        VStack(alignment: .leading, spacing: 8) {
                            ForEach(session.logs) { log in
                                HStack {
                                    Text(log.exerciseName)
                                    Spacer()
                                    Text(log.exerciseType == .strength ? "\(numberText(log.weight)) \(store.state.unit.rawValue)" : durationText((log.runSeconds + log.walkSeconds) * log.intervalRounds))
                                }
                            }
                            Button("DELETE SESSION", role: .destructive) { store.deleteSession(id: session.id) }
                                .font(.caption.bold())
                        }.padding(.top, 10)
                    } label: {
                        HStack {
                            Image(systemName: "folder.fill")
                            VStack(alignment: .leading) {
                                Text("\(first?.planName ?? "Workout") / \(first?.dayName ?? "Session")").font(.headline)
                                Text((first?.completedAt ?? Date()).formatted(date: .abbreviated, time: .shortened)).font(.caption).opacity(0.65)
                            }
                            Spacer()
                            Button(role: .destructive) { store.deleteSession(id: session.id) } label: { Image(systemName: "trash") }
                        }
                    }
                    .padding(14)
                    .overlay(RoundedRectangle(cornerRadius: 10).stroke(TerminalTheme(state: store.state).accent.opacity(0.35)))
                }
            }
            .padding()
        }
        .navigationTitle("FREE_WEIGHTS://IOS")
        .navigationBarTitleDisplayMode(.inline)
    }

    private func stat(_ label: String, _ value: String) -> some View {
        TerminalPanel {
            Text(value).font(.title.bold()).lineLimit(1).minimumScaleFactor(0.6)
            Text(label).font(.caption).opacity(0.65)
        }
    }
}

private struct RunDurationChart: View {
    @EnvironmentObject private var store: AppStore
    let logs: [WorkoutLog]

    private var latest: WorkoutLog { logs.max(by: { $0.completedAt < $1.completedAt })! }
    private var values: [(String, Int)] {
        let run = latest.runSeconds * latest.intervalRounds
        let walk = latest.walkSeconds * latest.intervalRounds
        return [("Run", run), ("Walk", walk), ("Total", run + walk)]
    }

    var body: some View {
        VStack(alignment: .leading) {
            Text("TOTAL SESSION \(durationText(values.last?.1 ?? 0))").font(.caption.bold())
            Chart(values, id: \.0) { item in
                BarMark(x: .value("Duration", item.1), y: .value("Segment", item.0))
                    .foregroundStyle(item.0 == "Total" ? TerminalTheme(state: store.state).accent : TerminalTheme(state: store.state).accent.opacity(0.55))
                .annotation(position: .trailing) { Text(durationText(item.1)).font(.caption) }
            }
            .frame(height: 150)
        }
    }
}
