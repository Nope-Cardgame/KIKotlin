# Nope - KI Spieler Kotlin

Repository für die Kotlin Clients und deren Schnittstellen.


## Dokumentation
[Technische Dokumentation](https://github.com/Nope-Cardgame/KIKotlin/tree/main/doc/TechnischeDokumentation.md)

[Coding Conventions](https://github.com/Nope-Cardgame/KIKotlin/tree/main/doc/CodingConventions.md)

[Doku Client1](https://github.com/Nope-Cardgame/KIKotlin/tree/main/doc/Client1Doku.md)

[Doku Client3](https://github.com/Nope-Cardgame/KIKotlin/tree/main/doc/Client3Doku.md)


## Mitglieder
Mitglied | entwickelter Client | 
--- | --- |
[Jonas Pollpeter](https://github.com/JonasPTFL) | Client1
[Tim Schlüter](https://github.com/TosSystems) | Client2 (Android App)
[Jan Rasche](https://github.com/Muquinbla) | Client3


## Installation

### Versionen
- Kotlin 1.8.0
- JDK 1.8

### Erklärung wie das Projekt ausgeführt wird

1. Installation von Kotlin und Java in den angegebenen Versionen 

    ggf. Android SDK 33 (für Client2 benötigt)  
2. Klonen des GitHub-Repo
3. Ausführen der kompletten Gradle-Skripte, danach erst das Programm ausführen (Einstiegspunkt: Main.kt)


## Benutzung
Beschreibung, wie das Programm bedient werden muss, um eine Verbindung zum NOPE-Server herzustellen (welche Konsoleneingaben/GUI Interaktionen müssen getätigt werden) und ein NOPE Spiel zu starten.

### Allgemein
Zur Ausführung eines bestimmten Client-Programms kann beim Start des Programms folgendes eingegeben werden: 
- "1": Client1 wird ausgeführt
- "3": Client3 wird ausgeführt
- "0" oder alles andere: beide Clients werden parallel ausgeführt. Dies kann zu Vermischungen der Ausgaben auf der Konsole führen. 


### Client1
Die Bedienung dieses Clients ist textbasiert über die Konsole aufgebaut. 

Nach dem Start des Clients wird der Benutzer aufgefordert, einen Benutzernamen und ein Passwort einzugeben.
Daraufhin versucht das Programm sich bei dem Server mti diesen Daten anzumelden. Sobald eine Verbindung hergestellt wurde, 
werden die aktiven Benutzerverbindungen auf der Konsole aufgelistet. Dann erhält der Benutzer die Möglichkeit, bestimmte Spieler
zu einem Nope-Spiel einzuladen oder diesen Schritt zu überspringen. Wenn der Benutzer selber eingeladen wird, muss er die Einladung
annehmen, um dem Spiel beizutreten. Sobald ein Spiel beendet ist, wird erneut gefragt Spieler einzuladen und ein neues spiel zu starten. 
Diese Abläufe sind über Konfigurations-Felder einstellbar. Das Programm kann auch so eingestellt werden, dass es nach der Anmeldung
automatisch Spieleinladungen annimmt und Spiele spielt.

### Client3
Die Ausgaben des Clients findet über die Konsole statt. Der Client verbindet sich automatisch mit dem Server und Spiel-/Tuniereinladungen werden automatisch angenommen.
