#!/bin/bash

# Отключаем нативный Wayland, используем XWayland
unset WAYLAND_DISPLAY

# Java настройки
export _JAVA_AWT_WM_NONREPARENTING=1
export AWT_TOOLKIT=MToolkit
export GDK_BACKEND=x11

# JavaFX настройки
export _JAVA_OPTIONS="-Dprism.order=sw -Dprism.verbose=true -Djava.awt.headless=false"

# Запуск приложения
cd ~/ProjectsJava/
mvn clean javafx:run
