Pod::Spec.new do |spec|
    spec.name                     = 'Cyborg'
    spec.version                  = '0.7'
    spec.homepage                 = 'Link to the Cyborg Module homepage'
    spec.source                   = { :git => "Not Published", :tag => "Cocoapods/#{spec.name}/#{spec.version}" }
    spec.authors                  = 'uber'
    spec.license                  = 'Apache-2.0'
    spec.summary                  = 'foo'
    spec.source_files             = 'Classes/**/*.{swift,h}'
    spec.public_header_files      = 'Classes/**/*.h'
    spec.summary                  = 'Some description for the Cyborg Module'
    spec.requires_arc             = true
    spec.ios.deployment_target    = '12.0'
    spec.swift_version            = '5.0'  # 根据你的需要选择适当的 Swift 版本
end