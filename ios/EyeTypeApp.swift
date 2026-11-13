import SwiftUI

@main
struct EyeTypeApp: App {
    var body: some Scene {
        WindowGroup {
            EyeTypeHomeView()
        }
    }
}

struct EyeTypeHomeView: View {
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 22) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("EyeType")
                            .font(.system(size: 38, weight: .bold, design: .rounded))
                            .foregroundStyle(Color(red: 0.20, green: 0.82, blue: 0.48))
                        Text("A keyboard designed for gaze and accessibility.")
                            .font(.title3)
                            .foregroundStyle(.secondary)
                    }

                    instructionCard(
                        number: "01",
                        title: "Add the EyeType keyboard",
                        detail: "Open Settings → General → Keyboard → Keyboards → Add New Keyboard, then choose EyeType."
                    )
                    instructionCard(
                        number: "02",
                        title: "Turn on Apple Eye Tracking",
                        detail: "Open Settings → Accessibility → Eye Tracking. Follow Apple's setup and enable Dwell Control if offered by your iOS version."
                    )
                    instructionCard(
                        number: "03",
                        title: "Use EyeType in any text field",
                        detail: "Switch to EyeType from the keyboard globe key. Apple's accessibility eye pointer can activate the keyboard buttons."
                    )

                    Text("iPhone privacy")
                        .font(.headline)
                    Text("The keyboard does not request camera access or Full Access. iPhone gaze interaction relies on Apple's built-in Accessibility Eye Tracking, not a camera running inside the keyboard extension.")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
                .padding(22)
            }
            .background(Color(red: 0.96, green: 0.98, blue: 0.96))
            .navigationTitle("Setup")
            .navigationBarTitleDisplayMode(.inline)
        }
        .tint(Color(red: 0.12, green: 0.55, blue: 0.30))
    }

    private func instructionCard(number: String, title: String, detail: String) -> some View {
        HStack(alignment: .top, spacing: 14) {
            Text(number)
                .font(.headline.monospacedDigit())
                .foregroundStyle(Color(red: 0.12, green: 0.55, blue: 0.30))
                .frame(width: 34, height: 34)
                .background(Color(red: 0.86, green: 0.95, blue: 0.88))
                .clipShape(Circle())
            VStack(alignment: .leading, spacing: 6) {
                Text(title).font(.headline)
                Text(detail).font(.subheadline).foregroundStyle(.secondary)
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}
