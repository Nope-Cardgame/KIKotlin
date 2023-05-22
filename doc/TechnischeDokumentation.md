# Technische Dokumentation Kotlin KI Schnittstelle

## Dateistruktur
Die Schnittstelle ist folgendermaßen strukturiert:
1. Package `entity`:
    - enthält alle Objekt-Model Klassen, die benötigt werden, um die JSON Daten von der REST- und Websocket-Schnittstelle zu parsen.
    - Objekte werden dann von den Clients als Datenstrukturen verwendet, um das Spiel zu analysieren und valide Züge zu spielen
    - Unterpaket action enthält die Entitäten, die zum Spielen von Aktionen benötigt werden
2. Package `rest`:
    - enthält Klassen, die für die REST Schnittstelle benötigt werden
3. `Constants`: Enthält String-Konstanten, die für die Verbindung zum Server und für die Websocket-Aktionen benötigt werden
4. `KotlinClientInterface`: Hauptschnittstelle für die Client-Implementierungen, organisiert und abstrahiert die REST-Api und Websocket-Verbindung
und ermöglicht das Spielen von Spielzügen
5. `NopeEventListener`: Interface, dass die Methoden/Events definiert, die von der Websocket-Verbindung ausgelöst werden
6. `NopeGame`: Interface, das von der Klasse KotlinClientInterface implementiert wird. Es stellt alle möglichen Aktionen/Aufrufe aus Client-Sicht dar. 
Mittels dieser definierten Schnittstelle ist es möglich alle benötigten Spielaktionen auszuführen, um ein Nope-Spiel zu spielen.
7. `SerializationHelper`: Enthält Hilfsmethoden zur Konvertierung von JSON-Text zu Kotlin-Klassen bzw. von Kotlin-Klassen zu JSON-Text mittels der Gson-Bibliothek
8. `SocketConnection`: enthält die Hauptlogik zur Verwaltung der Verbindung über SocketIO mittels der SocketIO-Java-Bibliothek. Versucht bei Initialisierung
automatisch eine Verbindung zum Server über SocketIO aufzubauen, empfängt die SocketIO-Events und leitet diese an den übergebenen NopeEventListener weiter

## Benutzung der Schnittstelle
1. Erstellen einer `KotlinClientInterface`-Instanz mit gewünschten Benutzername und Passwort (SignIn/SignUp passiert vollautomatisch)
2. An diese `KotlinClientInterface`-Instanz wird im Konstruktor eine Klassenreferenz übergeben, die das Interface NopeEventListener 
implementiert und auf SocketIO-Events reagiert.
3. Danach können über die `KotlinClientInterface`-Instanz alle gerade verbundenen Spieler erfahren werden und ein Nope-Spiel gestartet werden
4. Bei Erhalt eines Events über SocketIO muss dann gegebenenfalls ein gültiger Spielzug vom Client aus aufgerufen werden. 
Das Interface NopeGame spezifiziert die möglichen Methoden und Aktionen. Die `KotlinClientInterface`-Instanz implementiert dieses Interface und stellt 
die Funktionsweise für diese Aktionen bereit

## Hinweis zur Spielstart-Implementierung 
Die Benutzernamen der aktiven Benutzerverbindungen sind in der aktuellen (Stand 19.05.2023) Implementierung des Servers 
nicht eindeutig. Es empfiehlt sich, die Benutzer nach der eindeutigen `SocketID` zu filtern/auszuwählen. 
Zum Starten eines Nope-Spiels wird neben den Player-Objekten, die eingeladen werden sollen, auch das "eigene" Player Objekt 
benötigt und muss in der `player` Liste angegeben werden, um selber auch an dem neuen Spiel teilzunehmen.
Auch um das "eigene" `Player`-Objekt zu erhalten ist es sinnvoll, die aktiven Benutzerverbindungen über die `SocketID` nach dem "eigenen" 
Spieler Objekt zu filtern. Die eindeutige eigene `SocketID` kann über die Methode KotlinClientInterface.getSocketID erhalten werden.



