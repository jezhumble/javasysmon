define 'javasysmon' do |project|
  manifest['Main-Class'] = 'com.jezhumble.javasysmon.JavaSysMon'
  project.version = '0.1'
  package(:jar)
end
