dependencies {
    compileOnly(project(":game"))
    compileOnly(project(":shared"))
    compileOnly(fileTree("spigot"))
    compileOnly(project(":spigot-integration"))
    api(project(":persistentgames:housing-api"))
    compileOnly("com.github.koca2000:NoteBlockAPI:1.6.3")
}
