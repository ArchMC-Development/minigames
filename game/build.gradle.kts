repositories {
    maven("https://repo.infernalsuite.com/repository/maven-snapshots/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.lunarclient.dev")
}

dependencies {
    api(project(":shared"))

    api(project(":versioned:generics"))
    api(project(":versioned:legacy"))
    api(project(":versioned:modern"))

    compileOnly(project(":parties"))
    compileOnly(fileTree("spigot"))

    compileOnly("net.dmulloy2:ProtocolLib:5.4.0")
    compileOnly("com.lunarclient:apollo-api:1.1.8")

    val commonsVersion = property("commonsVersion")
    compileOnly("gg.scala.commons:modern-access:$commonsVersion")
    compileOnly("com.lunarclient:apollo-extra-adventure4:1.1.8")
}
