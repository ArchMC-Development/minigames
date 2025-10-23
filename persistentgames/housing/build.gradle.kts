dependencies {
    compileOnly(project(":game"))
    compileOnly(project(":shared"))
    compileOnly(fileTree("spigot"))
    compileOnly(project(":spigot-integration"))
    api(project(":microgames:bridging-api"))
}
