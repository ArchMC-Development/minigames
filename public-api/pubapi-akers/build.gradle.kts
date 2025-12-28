dependencies {
    compileOnly(project(":shared"))

    val commonsVersion = property("commonsVersion")
    compileOnly("gg.scala.commons:bukkit:$commonsVersion")
    compileOnly("gg.scala.store:spigot:1.0.0")
}
