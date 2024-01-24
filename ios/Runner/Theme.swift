//
//  Theme.swift
//  Runner
//
//  Created by vicky Leu on 2024/1/15.
//

import Foundation
import Cyborg

final class Theme: ColorProvider, Cyborg.ThemeProviding {
    
    func colorFromTheme(named name: String) -> UIColor {
        colorForKey(name)
    }
    
}

final class Resources: ColorProvider, ResourceProviding {
    
    func colorFromResources(named name: String) -> UIColor {
        colorForKey(name)
    }
    
}


class ColorProvider: Codable {
    
    private(set) var colors: [NamedColor] = []
    var mappedColors: [String: NamedColor] = [:] {
        didSet {
            colors = mappedColors.map { $0.value }
        }
    }
    
    func removeColor(at index: Int) {
        mappedColors[colors[index].name] = nil
    }
    
    func colorForKey(_ key: String) -> UIColor {
        if let color = mappedColors[key] {
            return UIColor(rgba: color.hex)
        } else {
            return .black
        }
    }
    
}

struct NamedColor: Codable {
    var name: String
    var hex: Int64
}

extension UIColor {
    
    convenience init(rgba value: Int64) {
        let alpha = CGFloat(value >> 24 & 0xff) / 255.0
        let red = CGFloat(value >> 16 & 0xff) / 255.0
        let green = CGFloat(value >> 8 & 0xff) / 255.0
        let blue = CGFloat(value & 0xff) / 255.0
        self.init(red: red, green: green, blue: blue, alpha: alpha)
    }

    
}

extension VectorDrawable {
    public static func named(_ name: String) -> VectorDrawable? {
        return Bundle.main.url(forResource: name, withExtension: "xml").flatMap { url in
            switch VectorDrawable.create(from: url) {
            case .ok(let drawable):
                return drawable
            case .error(let error):
                NSLog("Could not create a vectordrawable named \(name); the error was \(error)")
                return nil
            }
        }
    }
}
