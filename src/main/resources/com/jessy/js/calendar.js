/* ============================================================
    calendar.js
    Calendrier mensuel interactif pour la page Evenements.fxml.
   Chargé dans un JavaFX WebView.

   Pont Java <-> JS attendu :
     - Java appelle window.setEvenements(jsonArray) pour injecter les événements.
     - Java expose window.javaBridge avec les méthodes :
         javaBridge.onCreerEvenement(dateISO)
         javaBridge.onModifierEvenement(idEvenement)
         javaBridge.onSupprimerEvenement(idEvenement)
         javaBridge.onChangerMois(anneeAffichee, moisAffiche)  // 1-12

   Format attendu d'un événement injecté par Java :
   {
     id: "42",
     titre: "Cours de Java",
     dateDebut: "2026-09-18T10:00:00",
     dateFin:   "2026-09-18T12:00:00",
     lieu: "Salle B204",
     categorie: "cours" | "reunion" | "perso",
     recurrent: true|false
   }
   ============================================================ */

(function () {
    "use strict";

    const MOIS_FR = [
        "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
        "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
    ];

    let dateAffichee = new Date();
    let evenements = [];

    const grid = document.getElementById("grid");
    const monthLabel = document.getElementById("monthLabel");
    const popover = document.getElementById("eventPopover");
    const popTitle = document.getElementById("popTitle");
    const popTime = document.getElementById("popTime");
    const popLocation = document.getElementById("popLocation");
    const popEdit = document.getElementById("popEdit");
    const popDelete = document.getElementById("popDelete");

    let idEvenementSelectionne = null;

    // ---------- API appelée depuis Java ----------
    window.setEvenements = function (jsonArray) {
        try {
            evenements = typeof jsonArray === "string" ? JSON.parse(jsonArray) : jsonArray;
        } catch (e) {
            evenements = [];
        }
        render();
    };

    window.allerAuMois = function (annee, mois) {
        dateAffichee = new Date(annee, mois - 1, 1);
        render();
    };

    // ---------- Navigation ----------
    document.getElementById("btnPrev").addEventListener("click", () => {
        dateAffichee.setMonth(dateAffichee.getMonth() - 1);
        render();
        notifierChangementMois();
    });

    document.getElementById("btnNext").addEventListener("click", () => {
        dateAffichee.setMonth(dateAffichee.getMonth() + 1);
        render();
        notifierChangementMois();
    });

    document.getElementById("btnToday").addEventListener("click", () => {
        dateAffichee = new Date();
        render();
        notifierChangementMois();
    });

    function notifierChangementMois() {
        if (window.javaBridge && window.javaBridge.onChangerMois) {
            window.javaBridge.onChangerMois(dateAffichee.getFullYear(), dateAffichee.getMonth() + 1);
        }
    }

    // ---------- Rendu du calendrier ----------
    function render() {
        monthLabel.textContent = MOIS_FR[dateAffichee.getMonth()] + " " + dateAffichee.getFullYear();
        grid.innerHTML = "";

        const annee = dateAffichee.getFullYear();
        const mois = dateAffichee.getMonth();

        const premierJourMois = new Date(annee, mois, 1);
        // Lundi = 0 ... Dimanche = 6
        const decalage = (premierJourMois.getDay() + 6) % 7;

        const debutGrille = new Date(annee, mois, 1 - decalage);
        const aujourdHui = new Date();

        for (let i = 0; i < 42; i++) {
            const jourCourant = new Date(debutGrille);
            jourCourant.setDate(debutGrille.getDate() + i);

            const cell = document.createElement("div");
            cell.className = "day-cell";

            if (jourCourant.getMonth() !== mois) cell.classList.add("outside");
            if (estMemeJour(jourCourant, aujourdHui)) cell.classList.add("today");

            const numero = document.createElement("div");
            numero.className = "day-number";
            numero.textContent = jourCourant.getDate();
            cell.appendChild(numero);

            const eventsDuJour = document.createElement("div");
            eventsDuJour.className = "day-events";

            const evtsJour = evenementsPourJour(jourCourant);
            const maxAffiches = 3;

            evtsJour.slice(0, maxAffiches).forEach((evt) => {
                const chip = document.createElement("div");
                chip.className = "event-chip cat-" + (evt.categorie || "perso") + (evt.recurrent ? " recurrent" : "");
                chip.textContent = evt.titre;
                chip.addEventListener("click", (e) => {
                    e.stopPropagation();
                    afficherPopover(evt, e.clientX, e.clientY);
                });
                eventsDuJour.appendChild(chip);
            });

            if (evtsJour.length > maxAffiches) {
                const plus = document.createElement("div");
                plus.className = "more-events";
                plus.textContent = "+" + (evtsJour.length - maxAffiches) + " autre(s)";
                eventsDuJour.appendChild(plus);
            }

            cell.appendChild(eventsDuJour);

            // Clic sur une case vide => proposer de créer un événement ce jour-là
            cell.addEventListener("click", () => {
                masquerPopover();
                if (window.javaBridge && window.javaBridge.onCreerEvenement) {
                    window.javaBridge.onCreerEvenement(formatISO(jourCourant));
                }
            });

            grid.appendChild(cell);
        }
    }

    function evenementsPourJour(date) {
        return evenements.filter((evt) => {
            const d = new Date(evt.dateDebut);
            return estMemeJour(d, date);
        }).sort((a, b) => new Date(a.dateDebut) - new Date(b.dateDebut));
    }

    function estMemeJour(a, b) {
        return a.getFullYear() === b.getFullYear() &&
            a.getMonth() === b.getMonth() &&
            a.getDate() === b.getDate();
    }

    function formatISO(date) {
        const y = date.getFullYear();
        const m = String(date.getMonth() + 1).padStart(2, "0");
        const d = String(date.getDate()).padStart(2, "0");
        return y + "-" + m + "-" + d;
    }

    // ---------- Info-bulle événement ----------
    function afficherPopover(evt, x, y) {
        idEvenementSelectionne = evt.id;
        popTitle.textContent = evt.titre + (evt.recurrent ? "  ↻" : "");
        popTime.textContent = formatHeure(evt.dateDebut) + " – " + formatHeure(evt.dateFin);
        popLocation.textContent = evt.lieu || "";

        popover.classList.remove("hidden");
        const marge = 12;
        popover.style.left = Math.min(x + marge, window.innerWidth - 240) + "px";
        popover.style.top = Math.min(y + marge, window.innerHeight - 140) + "px";
    }

    function masquerPopover() {
        popover.classList.add("hidden");
        idEvenementSelectionne = null;
    }

    function formatHeure(iso) {
        const d = new Date(iso);
        return String(d.getHours()).padStart(2, "0") + "h" + String(d.getMinutes()).padStart(2, "0");
    }

    popEdit.addEventListener("click", () => {
        if (idEvenementSelectionne && window.javaBridge && window.javaBridge.onModifierEvenement) {
            window.javaBridge.onModifierEvenement(idEvenementSelectionne);
        }
        masquerPopover();
    });

    popDelete.addEventListener("click", () => {
        if (idEvenementSelectionne && window.javaBridge && window.javaBridge.onSupprimerEvenement) {
            window.javaBridge.onSupprimerEvenement(idEvenementSelectionne);
        }
        masquerPopover();
    });

    document.addEventListener("click", masquerPopover);

    // Rendu initial (grille vide en attendant les données de Java)
    render();
})();
