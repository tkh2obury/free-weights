import SwiftUI

extension Color {
    init(hex: String, fallback: Color = .green) {
        let clean = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        guard clean.count == 6, let value = UInt64(clean, radix: 16) else {
            self = fallback
            return
        }
        self = Color(
            red: Double((value >> 16) & 0xFF) / 255,
            green: Double((value >> 8) & 0xFF) / 255,
            blue: Double(value & 0xFF) / 255
        )
    }
}

struct TerminalTheme {
    let accent: Color
    let background: Color
    let panel: Color
    let muted: Color

    init(state: AppState) {
        accent = Color(hex: state.themeTextColor)
        background = Color(hex: state.themeBackgroundColor, fallback: .black)
        panel = accent.opacity(0.05)
        muted = accent.opacity(0.62)
    }
}

struct TerminalPanel<Content: View>: View {
    @EnvironmentObject private var store: AppStore
    let content: Content

    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }

    var body: some View {
        let theme = TerminalTheme(state: store.state)
        content
            .padding(14)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(theme.panel)
            .clipShape(.rect(topLeadingRadius: 0, bottomLeadingRadius: 14, bottomTrailingRadius: 0, topTrailingRadius: 14))
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(theme.accent.opacity(0.32), lineWidth: 1)
            )
    }
}

struct SectionTitle: View {
    @EnvironmentObject private var store: AppStore
    let text: String

    var body: some View {
        Text("> \(text.uppercased())")
            .font(.system(.title3, design: .monospaced, weight: .black))
            .foregroundStyle(TerminalTheme(state: store.state).accent)
            .frame(maxWidth: .infinity, alignment: .leading)
            .lineLimit(1)
    }
}

extension View {
    func terminalScreen(_ state: AppState) -> some View {
        let theme = TerminalTheme(state: state)
        return self
            .fontDesign(.monospaced)
            .foregroundStyle(theme.accent)
            .tint(theme.accent)
            .background(theme.background.ignoresSafeArea())
            .preferredColorScheme(.dark)
    }
}
