import UIKit

final class EyeTypeKeyboardViewController: UIInputViewController {
    private let rows = [
        ["Q","W","E","R","T","Y","U","I","O","P"],
        ["A","S","D","F","G","H","J","K","L"],
        ["Z","X","C","V","B","N","M","⌫"],
        ["123",",","SPACE",".","↵"]
    ]

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor(red: 0.06, green: 0.09, blue: 0.07, alpha: 1)
        buildKeyboard()
    }

    private func buildKeyboard() {
        let stack = UIStackView()
        stack.axis = .vertical
        stack.spacing = 7
        stack.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(stack)

        NSLayoutConstraint.activate([
            stack.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 5),
            stack.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -5),
            stack.topAnchor.constraint(equalTo: view.topAnchor, constant: 8),
            stack.bottomAnchor.constraint(equalTo: view.bottomAnchor, constant: -8)
        ])

        rows.forEach { row in
            let rowStack = UIStackView()
            rowStack.axis = .horizontal
            rowStack.spacing = 4
            rowStack.distribution = .fillEqually

            row.forEach { key in
                let button = UIButton(type: .system)
                button.setTitle(key, for: .normal)
                button.titleLabel?.font = .systemFont(ofSize: key.count > 1 ? 12 : 18, weight: .semibold)
                button.setTitleColor(.white, for: .normal)
                button.backgroundColor = UIColor(red: 0.15, green: 0.22, blue: 0.18, alpha: 1)
                button.layer.cornerRadius = 8
                button.accessibilityLabel = key == "SPACE" ? "Space" : key
                button.addAction(UIAction { [weak self] _ in self?.insert(key) }, for: .touchUpInside)
                rowStack.addArrangedSubview(button)
            }
            stack.addArrangedSubview(rowStack)
        }
    }

    private func insert(_ key: String) {
        switch key {
        case "⌫": textDocumentProxy.deleteBackward()
        case "SPACE": textDocumentProxy.insertText(" ")
        case "↵": textDocumentProxy.insertText("\n")
        case "123": return
        default: textDocumentProxy.insertText(key)
        }
    }
}
