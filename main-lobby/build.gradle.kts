dependencies {
    compileOnly(project(":lobby"))
    compileOnly(fileTree("spigot"))
    api(project(":public-api:pubapi-akers"))
    compileOnly("net.dmulloy2:ProtocolLib:5.4.0")
}
