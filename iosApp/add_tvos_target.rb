#!/usr/bin/env ruby
require 'xcodeproj'

project_path = File.join(__dir__, 'iosApp.xcodeproj')
proj = Xcodeproj::Project.open(project_path)

# Avoid duplicates on re-run
proj.targets.select { |t| t.name == 'tvOSApp' }.each(&:remove_from_project)
proj.main_group.children.select { |g| g.respond_to?(:display_name) && g.display_name == 'tvOSApp' }.each(&:remove_from_project)

target = proj.new_target(:application, 'tvOSApp', :tvos, '17.0', nil, :swift)

# Source files (classic group + references, not a synchronized group)
group = proj.main_group.new_group('tvOSApp', 'tvOSApp')
%w[TvOSApp TvContainer TvRootView TvConnectView TvLibraryView TvObservables TvAlbumTracksView TvZonesView TvArtwork TvSearchView].each do |name|
  ref = group.new_reference("#{name}.swift")
  target.source_build_phase.add_file_reference(ref)
end

# Asset catalog (tvOS App Icon & Top Shelf brand assets)
assets = group.new_reference('Assets.xcassets')
target.resources_build_phase.add_file_reference(assets)

# "Compile Kotlin Framework" — builds + embeds the tvOS SharedLogic framework.
# Must run before Compile Sources so `import SharedLogic` resolves.
phase = target.new_shell_script_build_phase('Compile Kotlin Framework')
phase.shell_script = <<~SH
  if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
    echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED set to YES"
    exit 0
  fi
  cd "$SRCROOT/.."
  ./gradlew :sharedLogic:embedAndSignAppleFrameworkForXcode
SH
target.build_phases.delete(phase)
target.build_phases.unshift(phase)

target.build_configurations.each do |config|
  bs = config.build_settings
  bs['SDKROOT'] = 'appletvos'
  bs['SUPPORTED_PLATFORMS'] = 'appletvos appletvsimulator'
  bs['TVOS_DEPLOYMENT_TARGET'] = '17.0'
  bs['TARGETED_DEVICE_FAMILY'] = '3'
  bs['SWIFT_VERSION'] = '5.0'
  bs['GENERATE_INFOPLIST_FILE'] = 'YES'
  bs['PRODUCT_NAME'] = '$(TARGET_NAME)'
  bs['PRODUCT_BUNDLE_IDENTIFIER'] = 'com.jrr.jrrkmp-native-ui.tvos'
  bs['LD_RUNPATH_SEARCH_PATHS'] = ['$(inherited)', '@executable_path/Frameworks']
  bs['ENABLE_USER_SCRIPT_SANDBOXING'] = 'NO'
  bs['ASSETCATALOG_COMPILER_GENERATE_ASSET_SYMBOLS'] = 'NO'
  bs['ASSETCATALOG_COMPILER_APPICON_NAME'] = 'App Icon & Top Shelf Image'
  # Default (ad-hoc "-") signing for the simulator. Disabling signing makes the
  # Kotlin embedAndSign task skip (no EXPANDED_CODE_SIGN_IDENTITY), which leaves
  # the SKIE-processed framework out of the build.
  bs['CODE_SIGN_STYLE'] = 'Automatic'
end

proj.save
puts "Added tvOSApp target with #{target.source_build_phase.files.count} sources."
