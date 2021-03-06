Hearthstone: Simple multiworld homes
====================================

Hearthstone is a plugin for the Minecraft wrapper [Bukkit](http://bukkit.org/) that allows players to set a single home per world and cooldowns between teleports. It also allows administrators to bypass the cooldown and teleport to any other players home. Additionally it prevents players from setting their home in regions that they are not allowed to build in.

## Features

- Each player can set one home per world
- Configurable cooldown between teleports.
- Able to teleport to any other player's home.
- Prevents players from teleporting into harmful things (like a block of stone)
- Prevents players from setting home locations in places where they are unable to build.
- Multilanguage support (even down to the command and permission nodes)

## License

Hearthstone is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

Hearthstone is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

## Documentation

If you are a server administrator, many of the features specific to Hearthstone are documented [on the wiki](https://github.com/grandwazir/Hearthstone/wiki). If you are looking to change the messages used in Hearthstone or localise the plugin into your own language you will want to look at [this page](https://github.com/grandwazir/BukkitUtilities/wiki/Localisation) instead.

If you are a developer you may find the [JavaDocs](http://grandwazir.github.com/Hearthstone/apidocs/index.html) and a [Maven website](http://grandwazir.github.com/Hearthstone/) useful to you as well.

## Installation

Before installing, you need to make sure you are running at least the latest [recommended build](http://dl.bukkit.org/latest-rb/craftbukkit.jar) for Bukkit. Support is only given for problems when using a recommended build. This does not mean that the plugin will not work on other versions of Bukkit, the likelihood is it will, but it is not supported.

### Getting the latest version

The best way to install Hearthstone is to use the [symbolic link](http://repository.james.richardson.name/symbolic/Hearthstone.jar) to the latest version. This link always points to the latest version of Hearthstone, so is safe to use in scripts or update plugins. A [feature changelog](https://github.com/grandwazir/BanHammer/wiki/changelog) is also available.

### Getting older versions

Alternatively [older versions](http://repository.james.richardson.name/releases/name/richardson/james/bukkit/hearthstone/) are available as well, however they are not supported. If you are forced to use an older version for whatever reason, please let me know why by [opening a issue](https://github.com/grandwazir/Hearthstone/issues/new) on GitHub.

### Building from source

You can also build BanHammer from the source if you would prefer to do so. This is useful for those who wish to modify BanHammer before using it. Note it is no longer necessary to do this to alter messages in the plugin. Instead you should read the documentation on how to localise the plugin instead. This assumes that you have Maven and git installed on your computer.

    git clone git://github.com/grandwazir/Hearthstone.git
    cd Hearthstone
    mvn install

## Reporting issues

If you are a server administrator and you are requesting support in installing or using the plugin you should [make a post](http://dev.bukkit.org/server-mods/hearthstone/forum/create-thread/) in the forum on BukkitDev. If you want to make a bug report or feature request please do so using the [issue tracking](https://github.com/grandwazir/Hearthstone/issues) on GitHub.
