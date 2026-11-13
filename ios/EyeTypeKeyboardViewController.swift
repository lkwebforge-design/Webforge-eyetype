import UIKit

final class EyeTypeKeyboardViewController: UIInputViewController {
    private var symbolsMode = false
    private var shiftMode = false

    private var letterRows: [[String]] {
        [
            ["Q","W","E","R","T","Y","U","I","O","P"],
            ["A","S","D","F","G","H","J","K","L"],
            ["⇧","Z","X","C","V","B","N","M","⌫"],
            ["123",",","SPACE",".","↵"]
        ]
    }

    private var symbolRows: [[String]] {
        [
            ["1","2","3","4","5","6","7","8","9","0"],
            ["@","#","$","%","&","-","+","(",")"],
            ["=","*","' ",":",";","!","?","~","⌫"],
            ["ABC",",","SPACE",".","↵"]
        ]
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor(red: 0.06, green: 0.09, blue: 0.07, alpha: 1)
        preferredContentSize = CGSize(width: 0, height: 270)
        buildKeyboard()
    }

    private func buildKeyboard() {
        view.subviews.forEach { $0.removeFromSuperview() }

        let stack = UIStackView()
        stack.axis = .vertical
        stack.spacing = 7
        stack.distribution = .fillEqually
        stack.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(stack)

        NSLayoutConstraint.activate([
            stack.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 5),
            stack.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -5),
            stack.topAnchor.constraint(equalTo: view.topAnchor, constant: 8),
            stack.bottomAnchor.constraint(equalTo: view.bottomAnchor, constant: -8),
            view.heightAnchor.constraint(equalToConstant: 270)
        ])

        (symbolsMode ? symbolRows : letterRows).forEach { row in
            let rowStack = UIStackView()
            rowStack.axis = .horizontal
            rowStack.spacing = 4
            rowStack.distribution = .fillEqually

            row.forEach { key in
                let title: String
                if key == "SPACE" { title = "English" }
                else if key.count == 1 && key.first?.isLetter == true && !shiftMode { title = key.lowercased() }
                else { title = key }

                let button = UIButton(type: .system)
                button.setTitle(title, for: .normal)
                button.titleLabel?.font = .systemFont(ofSize: key.count > 1 ? 12 : 18, weight: .semibold)
                button.setTitleColor(.white, for: .normal)
                button.backgroundColor = ["⌫", "↵", "⇧", "123", "ABC"].contains(key)
                    ? UIColor(red: 0.22, green: 0.33, blue: 0.27, alpha: 1)
                    : UIColor(red: 0.15, green: 0.22, blue: 0.18, alpha: 1)
                button.layer.cornerRadius = 8
                button.accessibilityLabel = accessibilityName(for: key)
                button.addAction(UIAction { [weak self] _ in self?.handle(key) }, for: .touchUpInside)
                rowStack.addArrangedSubview(button)
            }
            stack.addArrangedSubview(rowStack)
        }
    }

    private func accessibilityName(for key: String) -> String {
        switch key {
        case "SPACE": return "Space"
        case "⌫": return "Backspace"
        case "↵": return "Return"
        case "⇧": return "Shift"
        case "123": return "Numbers and symbols"
        case "ABC": return "Letters"
        default: return key
        }
    }

    private func handle(_ key: String) {
        switch key {
        case "⌫":
            textDocumentProxy.deleteBackward()
        case "SPACE":
            textDocumentProxy.insertText(" ")
        case "↵":
            textDocumentProxy.insertText("\n")
        case "123":
            symbolsMode = true
            buildKeyboard()
        case "ABC":
            symbolsMode = false
            buildKeyboard()
        case "⇧":
            shiftMode.toggle()
            buildKeyboard()
        default:
            textDocumentProxy.insertText(
                !symbolsMode && shiftMode ? key.uppercased() : key
            )
            if shiftMode {
                shiftMode = false
                buildKeyboard()
            }
        }
    }
}
