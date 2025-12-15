package mc.arch.minigames.persistent.housing.api.role

/**
 * ADMIN - Can do everything to house except delete it
 * READ - Can view all settings of house but not anything else
 * WRITE - Can write new settings to house AND edit them
 * EDIT - Can only edit or modify existing settings
 */
enum class RolePermissionLevel
{
    ADMIN, READ, WRITE, EDIT
}