repositories {
    maven("https://repo.infernalsuite.com/repository/maven-snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

configurations.all {
    exclude(group = "gg.scala.spigot", module = "server")
}

dependencies {
    compileOnly(project(":versioned:generics"))
    compileOnly("com.infernalsuite.asp:api:4.0.0-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
}
