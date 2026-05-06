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

    val commonsVersion = property("commonsVersion")
    compileOnly("gg.scala.commons:modern-access:$commonsVersion")

    // Sentry for distributed tracing
    compileOnly("io.sentry:sentry:7.22.0")
}
