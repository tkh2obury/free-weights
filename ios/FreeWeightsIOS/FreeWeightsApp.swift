import SwiftUI

@main
struct FreeWeightsApp: App {
    @StateObject private var store = AppStore()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(store)
        }
    }
}

struct RootView: View {
    @EnvironmentObject private var store: AppStore

    var body: some View {
        TabView {
            NavigationStack { WorkoutView() }
                .tabItem { Label("Workout", systemImage: "play.circle.fill") }
            NavigationStack { PlansView() }
                .tabItem { Label("Plans", systemImage: "folder.fill") }
            NavigationStack { ProgressView() }
                .tabItem { Label("Progress", systemImage: "chart.xyaxis.line") }
            NavigationStack { ToolsView() }
                .tabItem { Label("Tools", systemImage: "slider.horizontal.3") }
        }
        .terminalScreen(store.state)
    }
}
