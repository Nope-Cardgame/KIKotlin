# Technische Dokumentation Client3 - Jan Rasche

## Beschreibung/Bedienung Client3
Dieses Kotlin Programm ist dazu da, einen Client in einem Nope-Kartenspiel zu simulieren.
Der Spielablauf ist in der Konsole zu verfolgen.

## Dateistruktur
- `Client3Orga.kt` Hauptklasse und Einstiegspunkt für den Client1
- `Client3Logic.kt` Verwaltet "KI"/Entscheidungslogik, die während des Spiels von der Klasse `Client3Orga` aufgerufen werden
## Programmablauf
Bei Instanziierung eines Objektes der Klasse Client3Orga.kt werden die Authentifizierungs-Daten mitgegeben
und anschließend zur Anmeldung an dem Nope Server verwendet. Außerdem wird ein Logger Objekt erstellt, sodass alle
Log-Nachrichten in die Datei log_'username'.txt im Hauptverzeichnis geschrieben werden.

Nach Anmeldung und erfolgreicher Verbindung wird auf eine Spiel- oder Turniereinladung gewartet.
Diese wird automatisch angenommen.

Die wichtigste Methode während des Spielablaufs ist die von der Schnittstelle implementierten
Methode `gameStateUpdate`, die aufgerufen wird, wenn ein neuer Spielzustand aufgetreten ist.
In dieser Methode werden abhängig vom aktuellen Zustand Spiel-Aktionen entschieden und ausgeführt.

## KI - Logik
Zunächst werden die Handkarten nach einem der Farbe entsprechend passendem Set durchgegangen,
wenn keins gefunden wurde, wird entweder eine Karte verlangt oder "Nope" gesagt.
Wenn ein Set gefunden wurde, wird dieses nach der Farbanzahl sortiert (viele Farben nach oben).
Anschließend wird geprüft, ob eine zu den geforderten Farben passende Aktionskarte auf der hand ist.
Ist das der Fall wird die letzte gefundene Aktionskarte gespielt, ansonsten wird der zuvor gefundene Kartenstapel gelegt.
