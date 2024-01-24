//
//  LaunchScreenController.swift
//  Runner
//
//  Created by vicky Leu on 2024/1/3.
//

import Foundation
import UIKit
import Cyborg
class LaunchAnimController : UIViewController{
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.view.backgroundColor = UIColor.white
        
        let vectorView: VectorView = VectorView(theme: Theme(), resources: Resources())
        if let result = VectorDrawable.named("animated_lottie") {
            vectorView.drawable = result
        }
        vectorView.backgroundColor = UIColor.red
        self.view.addSubview(vectorView)
        NSLayoutConstraint
            .activate([
                vectorView.centerXAnchor.constraint(equalTo: self.view.centerXAnchor),
                vectorView.centerYAnchor.constraint(equalTo: self.view.centerYAnchor),
                vectorView.heightAnchor.constraint(equalToConstant: 100),
                vectorView.widthAnchor.constraint(equalToConstant: 120)
            ])
        
//        DispatchQueue.main.asyncAfter(deadline: .now()+3.3){
//            self.performSegue(withIdentifier: "openFlutterPage", sender: self)
//        }
    }
    
}
