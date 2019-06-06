# Handbuch zu TeamAndroid (Betreu-App)
### 1. Was ist TeamAndroid (Betreu-App)?
Dieses Projekt entstand im Auftrag der Hochschule für angewandte
Wissenschaften Ostfalia durch den Kurs "Anwendung mobiler Systeme".

Ziel dieses Projektes ist es, eine androidbasierte App zu entwickeln, die
eine kleine Zielgruppe hat. Dabei sollen wir Komponenten wie zum Beispiel Kammera,
Telefonie oder GPS-Tracking einbauen.

Diese App ist für die Betreuung von nicht zurechnungsfähigen Personen
gedacht. Dabei kann ein Betreuter mehr an Freiheiten gewinnen, indem der
Betreuer nicht immer in seiner Nähe sein muss. In der Praxis ist es der
Fall, dass der Betreute für jede Entscheidung die Erlaubnis des Betreuers
braucht. Dies ist sich vorzustellen wie ein kleines Kind, das
Süßigkeiten kaufen möchte, aber die Zustimmung der Mutter braucht. Diese
App soll das Genehmigen aus der Ferne erlauben. Damit wäre es eine
Steigernung der Lebensqualität, mindestens für den Betreuten.

Anzumerken ist noch, dass diese App ein Szenario durchspielt, welches
rechtlich nicht der Gesetzgebung entspricht. Daher ist dieses Projekt
ein Gedankenprojekt und ***darf bei dieser Rechtsprechung nicht im realen Leben 
zum Einsatz kommen!***

## 2. Wie richte ich diese App ein?
Das Einrichten dieser App findet vom Betreuer statt. Beim erstmaligen
Starten der App öffnet sich ein Login-Fenster. Dort kann die Rolle
ausgewählt werden, welche eingerichtet werden soll. Dabei gibt es zwei
Entscheidungsmöglichkeiten:  
* Betreuer
* Betreuter 

Diese werden auf zwei verschiedene Weisen eingerichtet.

#### 2.1 Einrichtung des Betreuers
Wählen Sie aus dem Radio-Button-Menü den Punkt "Betreuer" aus. Tippen
Sie daraufhin auf den Fab-Button (der runde Button unten rechts mit dem
Pfeil), um auf die nächste Seite zu gelangen. Sie werden daraufhin auf
die zukünftige Startseite weitergeleitet.

#### 2.2 Einrichtung des Betreuten
Wählen Sie aus dem Radio-Button-Menü den Punkt "Betreuter" aus.

Durch das Wählen des Radio-Buttons erscheinen zwei Eingabefelder. In einem steht 
"Sicherheitspasswort für Betreuer" und im anderen steht "Eigene Telefonnummer". Unter diesen
befindet sich ein Info-Button. Dieser Info-Button gibt Ihnen Auskünfte, wofür diese Eingabefelder
benötigt werden.

Das "Masterpasswort" ist zum Verändern von empfindlichen Daten wie zum
Beispiel die Kontaktinformationen des Betreuers da. Wo Sie diese
Informationen verändern können, wird zu einem späteren Zeitpunkt
beschrieben. Bitte suchen Sie sich ein sicheres Passwort aus. Bei der Erstellung
eines Passwortes sollten die Punkte des folgenden Links beachtet werden:
[hier](https://www.security-insider.de/fuenf-regeln-fuer-sichere-passwoerter-a-393490/)


Bestimmen Sie das Passwort und schreiben Sie es sich gut auf, denn
dieses lässt sich nicht mehr ändern. Wenn Sie mit dem Passwort zufrieden sind,
können Sie auf den Fab-Button drücken (der runde Button unten rechts mit dem Pfeil).
Mit diesem Button kommen Sie auf die nächste Seite.

Die nun weitergeleitete Seite ist für die Einstellung der Kontaktdaten
des Betreuers da. Diese sind wichtig, da für einen Anruf des Betreuers
seine Telefonnummer gebraucht wird. Der volle Name, inkluse Anschrift, ist
wichtig, falls dem Betreuten etwas geschieht. So könnten Außenstehende
Sie einfach anrufen, oder sie könnten den Betreuten im schlimmsten Fall
zu Ihnen liefern. Daher geben Sie bitte diese Angaben richtig an. Es ist
zum Besten Aller. Diese Daten können später mit dem vorher gewählten
"Masterpasswort" geändert werden. Wie dies geht erfahren Sie später. Fahren Sie
nach dem Ausfüllen aller relevanten Informationen mit dem Fab-Button
(der runde Button unten rechts mit dem Pfeil) fort.


Daraufhin ist die Einrichtung des Betreuten abgeschlossen und Sie
befinden sich auf der Startseite. Die Startseite wird von nun an bei
jedem Starten der App als Startseite angezeigt, wenn der Betreute die 
App startet.

#### 2.3 Anmerkungen
Wenn sie die falsche Rolle eingeloggt haben, kommen Sie auf normalen Wegen
nicht wieder zum Login zurück. Das ist beabsichtigt, da sonst der
Betreute seine Rolle ändern könnte. Wenn Sie sich aber bei der
Rollenverteilung vertan haben, installieren Sie die App neu.

## 3 Hauptseite
#### 3.1 Betreuer 
Diese Ansicht ist Ihre normale Startansicht. Wenn Sie auf der Startseite
sind, sehen sie drei Wesentliche Komponenten:

Es gibt fünf Buttons. Der Button oben rechts zeigt Informationen, welche Sie später
eventuell benötigen. Ein grüner Button mit der Beschriftung "Anrufen" steht unten links. Wenn
Sie diesen Button drücken starten Sie einen Anruf zu dem oben
ausgewählten Betreuten und das Genehmigungsverfahren beginnt. Sehen Sie
sich bei genaueren Informationen 4.1 (Anrufsbildschirm) an. Durch das
Drücken des "Anrufen"-Button wechsel Sie dann auf den Anrufsbildschirm.

Die restlichen drei Buttons werden benötigt, um Betreute anzulegen, zu editieren 
oder zu löschen. Wenn ein Betreuter ausgewählt ist, werden die Informationen 
über diesen angezeigt.


#### 3.2 Betreuter
Auf der Hauptansicht des Betreuten befinden sich zwei wesentliche Komponenten:
 
Die Eine sind die allgemeinen Informationen über Ihren Betreuer. Dort stehen
Informationen wie der Name der Straße, Hausnummer, Postleitzahl und der
Name der Stadt, in der der Betreuer lebt. Beim dreimaligen Klicken, mit
maximal zwei Sekunden Abstand, auf den Namen des Betreurers und das
Eingeben des Masterpassworts können Sie die Informationen über den
Betreuer ändern.

Die zweite Komponente ist ein grüner Button. Wenn Sie diesen Button
drücken, starten Sie einen Anruf zu dem Betreuer. Von dort aus können Sie
sich mit ihm unterhalten und auch Fotos an dem Betreuer senden, die Sie
während des Anrufs aufnehmen. Sehen Sie sich bei genaueren Informationen
4.2 (Anrufsbildschirm) an. Durch das Drücken des "Anrufen"-Buttons,
wechseln Sie dann auf den Anrufsbildschirm.
 
## 4 Anrufsbildschirm
#### 4.1 Betreuer
Beim aufgebauten Anruf sehen Sie folgende Elemente:

Ein Kamerasymbol. Dieser ist ein Platzhalter. Wenn der Betreute ein
Foto aufgenommen und abgesendet hat, erscheint es an der Stelle des Platzhalters.
Das Foto erscheint beim Betreuer erst dann, wenn er auf den "Aktualisieren"-Button
gedrückt hat.

Mit zwei zusätzlichen Buttons kann eine Anfrage angenommen oder abgelehnt werden.
Diese Entscheidung wird als Text bei dem Betreuten dargestellt, wenn dieser auf den 
"Aktualisieren"-Button gedrückt hat.

Wenn auf den roten Button gedrückt wird, wird der Anruf beendet 
und das Startmenü öffnet sich wieder.

#### 4.2 Betreuter
Beim aufgebauten Anruf sehen Sie folgende Elemente:

Ein Kamerasymbol. Dieses ist ein Button. Wenn Sie auf dieses Bild
drücken, öffnet sich die Kamera. Daraufhin können Sie ein Bild aufnehmen
und es mit dem Haken bestätigen. Daraufin wird das Bild zu Ihrem
Betreuer gesendet. Er darf dann den Inhalt des Bildes genehmigen oder
ablehnen.

Ein roter Button. Wenn Sie diesen Button drücken, beendet sich der Anruf.

Wenn Sie auf den "Aktualisieren"-Button drücken, wird Antwort auf Ihre letzte 
Anfrage angezeigt, insofern bereits eine gegeben wurde.
