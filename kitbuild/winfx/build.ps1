#######################################################################################################################
# Settings
#######################################################################################################################

Set-Variable -Name REPO -Value C:\Home\Development\M2-Repository

Set-Variable -Name WORKINGDIR -Value "target"
Set-Variable -Name SOURCEDIR -Value "../../"

Set-Variable -Name PUBLIC_NAME -Value FlightSimLog
Set-Variable -Name PUBLIC_VERSION -Value "0.0.1"

Set-Variable -Name MAIN_MODULE -Value ch.nostromo.flightsimlog
Set-Variable -Name MAIN_CLASS -Value ch.nostromo.flightsimlog.FlightSimLog
Set-Variable -NAME ARTIFACT -Value "ch\nostromo\flightsimlog\0.0.1-SNAPSHOT\flightsimlog-0.0.1-SNAPSHOT.jar"

Set-Variable -Name UUID -Value "17d0c55b-35a7-4f1e-8a61-5e73a5e48b86"

Set-Variable -Name EXTERNAL_MODULES -Value @(
 "$REPO\$ARTIFACT"
 "$REPO\org\openjfx\javafx-base\17.0.13\javafx-base-17.0.13-win.jar"
 "$REPO\org\openjfx\javafx-controls\17.0.13\javafx-controls-17.0.13-win.jar"
 "$REPO\org\openjfx\javafx-fxml\17.0.13\javafx-fxml-17.0.13-win.jar"
 "$REPO\org\openjfx\javafx-graphics\17.0.13\javafx-graphics-17.0.13-win.jar"
 "$REPO\org\openjfx\javafx-media\17.0.13\javafx-media-17.0.13-win.jar"
 "$REPO\org\openjfx\javafx-web\17.0.13\javafx-web-17.0.13-win.jar"
 "$REPO\org\lembeck\simconnect-java-util\0.1-M\simconnect-java-util-0.1-M.jar"
 "$REPO\com\fasterxml\jackson\core\jackson-core\2.10.0\jackson-core-2.10.0.jar"
 "$REPO\com\fasterxml\jackson\core\jackson-databind\2.10.0\jackson-databind-2.10.0.jar"
 "$REPO\com\fasterxml\jackson\core\jackson-annotations\2.10.0\jackson-annotations-2.10.0.jar"
 "$REPO\jakarta\xml\bind\jakarta.xml.bind-api\2.3.2\jakarta.xml.bind-api-2.3.2.jar"
 "$REPO\jakarta\activation\jakarta.activation-api\1.2.2\jakarta.activation-api-1.2.2.jar"
 "$REPO\org\glassfish\jaxb\jaxb-runtime\2.3.2\jaxb-runtime-2.3.2.jar"
 "$REPO\org\glassfish\jaxb\txw2\2.3.2\txw2-2.3.2.jar"
 "$REPO\com\github\depsypher\pngtastic\1.7-SNAPSHOT-M\pngtastic-1.7-SNAPSHOT-M.jar"
 "$REPO\de\grundid\opendatalab\geojson-jackson\1.15-SNAPSHOT-M\geojson-jackson-1.15-SNAPSHOT-M.jar"
 "$REPO\org\jvnet\staxex\stax-ex\1.8.1\stax-ex-1.8.1.jar"
 "$REPO\com\sun\istack\istack-commons-runtime\3.0.8\istack-commons-runtime-3.0.8.jar"
 "$REPO\com\sun\xml\fastinfoset\FastInfoset\1.2.16\FastInfoset-1.2.16.jar"
)

Set-Variable -Name MODPATH -Value target
ForEach ($i in $EXTERNAL_MODULES) {
   $MODPATH += ";"
   $MODPATH += $i
}

#######################################################################################################################
# Running Build
#######################################################################################################################

$STARTDIR = pwd | Select-Object | %{$_.ProviderPath}

if (Test-Path $WORKINGDIR) { 
  Remove-Item $WORKINGDIR -Recurse -force; 
}
new-item $WORKINGDIR -itemtype directory > $null

cd $WORKINGDIR

Write-Output "Jlink ..."
& jlink.exe --module-path $MODPATH --add-modules $MAIN_MODULE --add-modules jdk.crypto.ec --launcher $PUBLIC_NAME=$MAIN_MODULE/$MAIN_CLASS --output app-vmimage

Write-Output "App image ..."
& jpackage.exe --type app-image --name $PUBLIC_NAME --module $MAIN_MODULE/$MAIN_CLASS --runtime-image app-vmimage

Write-Output "Installer ..."
& jpackage.exe --type msi --name $PUBLIC_NAME --app-version $PUBLIC_VERSION --win-shortcut --win-menu --win-upgrade-uuid $UUID --module-path $MODPATH --module $MAIN_MODULE/$MAIN_CLASS

cd $STARTDIR
