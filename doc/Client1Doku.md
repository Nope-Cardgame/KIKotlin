# Technische Dokumentation Client1 - Jonas Pollpeter

## Beschreibung/Bedienung Client1
Das Kotlin Programm ist eine Konsolen-Applikation, dass ein Nope-Kartenspiel ohne Benutzereingaben
spielen kann. Das Programm enthält einige in Bezug auf das Kartenspiel Nope sinnvolle Logiken,
um die Wahrscheinlichkeit zu erhöhen, das Nope-Spiel zu gewinnen.

Das Programm kann über statische Konfigurationsfelder in Bezug auf die Spiel-/Tuniererstellung eingestellt werden.
Dadurch ist es beispielsweise möglich, dass das Programm beim Start dem Benutzer ermöglicht, ein Tunier/Spiel mit 
beliebigen aktuell verbundene Spielern zu spielen.
Auch kann eingestellt werden, ob Spiel-/Tuniereinladungen automatisch angenommen werden, oder eine Bestätigung per 
Konsole erfordert ist. Des Weiteren ist es möglich über Konfigurationsfelder die Parameter/Optionen
bei der Spiel-/Tuniererstellung anzupassen.

## Dateistruktur
- `Client1.kt` Hauptklasse und Einstiegspunkt für den Client1
- `Client1GameLogic.kt` Verwaltet "KI"/Entscheidungslogik, die während des Spiels von der Klasse `Client1` aufgerufen werden
- `Console.kt` enthält Hilfsfunktionen für die Ein-/Ausgabe auf der Konsole
- `Helper.kt` enthält Hilfsfunktionen für Kotlin Datentypen und für Entitäts-Klassen der gemeinsamen Kotlin Schnittstelle


## Programmablauf
Bei Instanziierung eines Objektes der Klasse Client1.kt werden die Authentifizierungs-Daten über die Konsole abgefragt
und anschließend zur Anmeldung an dem Nope Server verwendet. Außerdem wird ein Logger Objekt erstellt, sodass alle 
Log-Nachrichten in die Datei log.txt im Hauptverzeichnis geschrieben werden.

Sobald die Anmeldung erfolgt ist, wird eine Socket Verbindung aufgebaut. Bei erfolgreicher Verbindung wird 
abhängig von den Konfigurationsfeldern das Erstellen eines Tuniers oder Spiels mit beliebigen Spielern ermöglicht oder
auf eine Spiel-/Tuniereinladung gewartet.

Die wichtigste Methode während des Spielablaufs ist die von der Schnittstelle implementierten 
Methode `gameStateUpdate`, die aufgerufen wird, wenn ein neuer Spielzustand aufgetreten ist. 
In dieser Methode werden abhängig vom aktuellen Zustand Spiel-Aktionen entschieden und ausgeführt.

Die Erklärungen, die bei den Spielaktionen von diesem Client mitgesendet werden, sind ebenfalls als Konstanten in dem 
Objekt `Explanation` verwaltet.

## KI - Logik
Die Klasse Client1GameLogic stellt Bewertungs- und Filterfunktionen für Spielelemente wie Karten, Spieler oder die Anzahl 
an Karten, die ein Spieler bei einer Auswahlkarte ablegen muss, bereit. Über einen "Evaluator" werden Spiel-Karten
bewertet und anschließend in den jeweiligen Methoden sortiert, sodass die besten Karten abgelegt werden können.
Über zwei Konfigurations-Konstanten kann die Bewertungsfunktion des Evaluator eingestellt/angepasst werden. 

Wenn der Spieler am Zug ist, wird grundsätzlich versucht eine passende Aktionskarte zu finden. Anschließend wird nach 
einer Zahlenkarte gesucht, die abgelegt werden kann. Ist keines von beidem möglich, wird entweder eine Karte gezogen 
oder die "Nope!" Aktion gesendet. 
