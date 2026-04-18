dependencies {
    compileOnly(project(":game"))
    compileOnly(project(":shared"))
    compileOnly(fileTree("spigot"))
    compileOnly(project(":spigot-integration"))
    api(project(":persistentgames:prison-shared"))
    compileOnly("gg.scala.basics.plugin:scala-basics-plugin:1.1.3")
}
