      // ------------------------------------------------------------------
      // Funktioner til at vise/skjule formularer på sponsorsiden
      // ------------------------------------------------------------------
      // toggleSponsorEdit: viser/skjuler inline sponsor-redigeringsformularen
      function toggleSponsorEdit(i) {
        const id = "sponsorEditForm-" + i;
        const el = document.getElementById(id);
        if (!el) return;
        el.style.display = el.style.display === "none" || el.style.display === "" ? "block" : "none";
      }
      // toggleContractEdit: viser/skjuler inline kontrakt-redigeringsformularen
      function toggleContractEdit(i) {
        const id = "contractEditForm-" + i;
        const el = document.getElementById(id);
        if (!el) return;
        el.style.display = el.style.display === "none" || el.style.display === "" ? "block" : "none";
      }
      // Selv-eksekverende funktion: sætter event-handlere op for sidefunktioner
      (function () {
        // attachPhoneHandlers: sikrer at telefon-input kun indeholder cifre (altså tal)
        function attachPhoneHandlers(el) {
          if (!el) return;
          el.addEventListener("keydown", function (e) {
            const k = e.key;
            if (k.length !== 1) return;
            if (!/^[0-9]$/.test(k)) {
              e.preventDefault();
            }
          });
          el.addEventListener("paste", function (e) {
            e.preventDefault();
            const text = (e.clipboardData || window.clipboardData).getData("text") || "";
            const filtered = text.replace(/[^0-9]/g, "");
            const start = el.selectionStart;
            const end = el.selectionEnd;
            const before = el.value.slice(0, start);
            const after = el.value.slice(end);
            el.value = before + filtered + after;
            const caret = start + filtered.length;
            el.setSelectionRange(caret, caret);
          });
          el.addEventListener("input", function () {
            const cleaned = el.value.replace(/[^0-9]/g, "");
            if (cleaned !== el.value) {
              const pos = Math.max(0, el.selectionStart - (el.value.length - cleaned.length));
              el.value = cleaned;
              el.setSelectionRange(pos, pos);
            }
          });
        }
        // Hæft telefon-validering på formularfelter
        attachPhoneHandlers(document.getElementById("phoneNumber"));
        document.querySelectorAll('input[name="phoneNumber"]').forEach(attachPhoneHandlers);

        // applySponsorFilter: filtreer sponsors baseret på værdien af dropdownen
        // - 'active' viser kun sponsorer med status true
        // - 'inactive' viser kun sponsorer med status false
        // - 'all' viser alle sponsorer
        function applySponsorFilter() {
          const sel = document.getElementById('sponsorFilter');
          if (!sel) return;
          const v = sel.value;
          document.querySelectorAll('.sponsor-item').forEach(function (el) {
            const status = el.getAttribute('data-status');
            if (v === 'all') {
              el.style.display = '';
            } else if (v === 'active') {
              el.style.display = status === 'true' || status === 'True' ? '' : 'none';
            } else if (v === 'inactive') {
              el.style.display = status === 'false' || status === 'False' ? '' : 'none';
            }
          });
        }

        // Finder filter-dropdownen (filterEl), sætter default til 'active', filtrerer listen ved load,
        // og aktiverer filtrering når brugeren ændrer dropdown-værdien
        const filterEl = document.getElementById('sponsorFilter');
        if (filterEl) {
          filterEl.value = 'active';
          applySponsorFilter();
          filterEl.addEventListener('change', applySponsorFilter);
        }
      })();

      // ------------------------------------------------------------------
      // Modal-hjælpere (bruges når brugeren klikker på knapperne i sponsor-boxen)
      // ------------------------------------------------------------------
      // openAddModal: fylder skjult felt med sponsor-id og viser tilføj-modal
      // Denne funktion kaldes fra hver sponsors "Tilføj Kontrakt"-knap
      // buttonEl - knappen der blev klikket (har data-sponsor-id/name)
      function openAddModal(buttonEl) {
        try {
          const sid = buttonEl.getAttribute('data-sponsor-id');
          const sname = buttonEl.getAttribute('data-sponsor-name') || '';
          document.getElementById('addModalSponsorId').value = sid;
          document.getElementById('addModalSponsorName').textContent = sname;
          document.getElementById('addContractModal').style.display = 'flex';
        } catch (e) {
          // Log fejl i konsollen hvis noget går galt; dette bør ikke kastes videre til bruger.
          console.error('openAddModal error', e);
        }
      }

      // openViewModal: viser modal med liste over kontrakter for valgt sponsor
      // Funktionen henter sponsor-id fra knappen og bygger indholdet via populateViewContracts
      function openViewModal(buttonEl) {
        try {
          const sid = buttonEl.getAttribute('data-sponsor-id');
          const sname = buttonEl.getAttribute('data-sponsor-name') || '';
          document.getElementById('viewModalSponsorName').textContent = sname || ('#' + sid);
          populateViewContracts(sid);
          document.getElementById('viewContractsModal').style.display = 'flex';
        } catch (e) {
          // Vis fejl i konsollen — modal-oprettelse må ikke give et uncaught exception
          console.error('openViewModal error', e);
        }
      }

      function closeModal(id) {
        const el = document.getElementById(id);
        if (!el) return;
        el.style.display = 'none';
      }

      // populateViewContracts: bygger og indsætter HTML for hver kontrakt
      // - Læser data fra #contractsData (server-renderet skjult DOM)
      // - Bygger en kort visning (Type, Start, End, Payment, Status)
      // - Tilføjer en 'Edit' knap som viser en indlejret form til at gemme ændringer
      // Param: sponsorId (String/Number) - id på den sponsor vi skal vise kontrakter for
      function populateViewContracts(sponsorId) {
        const listEl = document.getElementById('viewContractsList');
        if (!listEl) return;
        listEl.innerHTML = '';
        const container = document.getElementById('contractsData');
        if (!container) return;
        const nodes = container.querySelectorAll('.contract-data');
        const matches = [];
        nodes.forEach(function (n) {
          if (String(n.getAttribute('data-sponsor-id')) === String(sponsorId)) {
            matches.push(n);
          }
        });
        if (matches.length === 0) {
          listEl.innerHTML = '<div class="text-sm text-gray-600">Ingen kontrakter for denne sponsor.</div>';
          return;
        }
        const sponsorDisplayName = (document.getElementById('viewModalSponsorName') || {}).textContent || '';
        // konventere ISO-like dato (yyyy-mm-dd til dd-mm-yyyy for display
        function formatDisplayDate(d) {
          if (!d && d !== 0) return '';
          const s = String(d);
          const m = s.match(/^(\d{4})-(\d{2})-(\d{2})/);
          if (m) return `${m[3]}-${m[2]}-${m[1]}`;
          const dt = new Date(s);
          if (!isNaN(dt.getTime())) {
            const dd = String(dt.getDate()).padStart(2, '0');
            const mm = String(dt.getMonth() + 1).padStart(2, '0');
            const yyyy = dt.getFullYear();
            return `${dd}-${mm}-${yyyy}`;
          }
          return s;
        }

        matches.forEach(function (n, idx) {
          const id = n.getAttribute('data-id');
          const name = n.getAttribute('data-name') || '';
          const type = n.getAttribute('data-type') || '';
          const start = n.getAttribute('data-start') || '';
          const end = n.getAttribute('data-end') || '';
          const formattedStart = formatDisplayDate(start);
          const formattedEnd = formatDisplayDate(end);
          const payment = n.getAttribute('data-payment') || '';
          const status = n.getAttribute('data-status') || '';
          const pdfFileName = n.getAttribute('data-pdf-filename') || '';

          const item = document.createElement('div');
          item.className = 'border rounded p-2';
         
          // Opretter et container-element som indeholder hele kontrakt-blokken (titel, detaljer og edit-form)
          const container = document.createElement('div');
          container.className = 'border rounded p-2';

          // ----------------------------------------------------------
          // TOP-LINJE: Kontraktnavn + ID + knapper
          // ----------------------------------------------------------
          const topRow = document.createElement('div');
          topRow.className = 'flex justify-between items-center';

          // wrapper til navn + ID
          const title = document.createElement('div');
          title.className = 'font-medium';
          // kontraktens navn (tekst)
          const nameText = document.createElement('span');
          nameText.textContent = name;
          // kontraktens ID i parentes
          const idSpan = document.createElement('span');
          idSpan.className = 'text-xs text-gray-500';
          idSpan.textContent = ` (#${id})`;
          // Saml navn og ID
          title.appendChild(nameText);
          title.appendChild(idSpan);

          // gruppe til de knapper:  Edit, Slet
          const btnGroup = document.createElement('div');
          btnGroup.className = 'flex space-x-2';

          //knap til download pdf
            const btnDownload = document.createElement('button');
            btnDownload.type = 'button';
            btnDownload.className = 'px-2 py-1 text-sm bg-green-300 rounded';
            btnDownload.textContent = pdfFileName ?  `Download: ${pdfFileName}` : 'Download PDF';

            btnDownload.onclick = function () {
                const pdfAvailable = n.getAttribute('data-has-pdf');

                if (pdfAvailable === 'true') {
                    window.open(`/getFile/${id}`, '_blank');
                } else {
                    alert('Ingen PDF tilgængelig for denne kontrakt.');
                }
            };


          // knap: Åbn/luk redigeringsformular
          const btnEdit = document.createElement('button');
          btnEdit.type = 'button';
          btnEdit.className = 'px-2 py-1 text-sm bg-yellow-200 rounded';
          btnEdit.textContent = 'Rediger Kontrakten';

          // knap: Slet kontrakt (viser confirm-besked før sletning)
          const btnDelete = document.createElement('button');
          btnDelete.type = 'button';
          btnDelete.className = 'px-2 py-1 text-sm bg-red-300 rounded';
          btnDelete.textContent = 'Slet Kontrakt';

          // dette tilføjer knapperne
          const btnAddService = document.createElement('button');
          btnAddService.type = 'button';
          btnAddService.className = 'px-2 py-1 text-sm bg-blue-500 text-white rounded';
          btnAddService.textContent = 'Tilføj Tjeneste';

          // dette tilføjer knapperne 
          btnGroup.appendChild(btnEdit);
          btnGroup.appendChild(btnDelete);

          // og dette tilføjer titel + knapgruppe til top-linjen
          topRow.appendChild(title);
          topRow.appendChild(btnGroup);


          // ----------------------------------------------------------
          // DETALJE-OMRÅDE (Type, datoer, betaling, status)
          // ----------------------------------------------------------
          const details = document.createElement('div');
          details.id = `details-${idx}`;
          details.style.display = 'block';
          details.className = 'mt-2 text-sm text-gray-700';
          // individuelle detailfelter
          const dType = document.createElement('div'); dType.textContent = `Type: ${type}`;
          const dStart = document.createElement('div'); dStart.textContent = `Start: ${formattedStart}`;
          const dEnd = document.createElement('div'); dEnd.textContent = `End: ${formattedEnd}`;
          const dPayment = document.createElement('div'); dPayment.textContent = `Payment: ${payment}`;
          const dStatus = document.createElement('div'); dStatus.textContent = `Status: ${status}`;
          // Saml detailfelter
          details.appendChild(dType);
          details.appendChild(dStart);
          details.appendChild(dEnd);
          details.appendChild(dPayment);
          details.appendChild(dStatus);
          // placeringen på download pdf-filen
          details.appendChild(btnDownload);

          // ----------------------------------------------------------
          // SERVICES-LISTE: vis tjenester som hører til denne kontrakt
          // ----------------------------------------------------------
          const servicesWrapper = document.createElement('div');
          servicesWrapper.className = 'mt-3';

          // Header: title
          const servicesHeader = document.createElement('div');
          servicesHeader.className = 'flex items-center justify-between';
          const servicesTitle = document.createElement('div');
          servicesTitle.className = 'text-sm font-medium mb-2';
          servicesTitle.textContent = 'Tjenester';
          servicesHeader.appendChild(servicesTitle);
          // place the add-service button inline with the title
          btnAddService.className = 'px-3 py-1 text-sm bg-blue-500 text-white rounded';
          servicesHeader.appendChild(btnAddService);
          servicesWrapper.appendChild(servicesHeader);

          // container for the service cards (always visible)
          const servicesList = document.createElement('div');
          servicesList.className = 'mt-2';
          servicesList.style.display = 'block';
          servicesWrapper.appendChild(servicesList);

          // find services from hidden template
          try {
            const servicesContainer = document.getElementById('servicesData');
              if (servicesContainer) {
              const serviceNodes = servicesContainer.querySelectorAll('.service-data');
              serviceNodes.forEach(function (sn) {
                if (String(sn.getAttribute('data-contract-id')) === String(id)) {
                  const sid = sn.getAttribute('data-id');
                  const sname = sn.getAttribute('data-name') || '';
                  const stype = sn.getAttribute('data-type') || '';
                  const sstart = sn.getAttribute('data-start') || '';
                  const send = sn.getAttribute('data-end') || '';
                  const samount = sn.getAttribute('data-amount') || '';
                  const sarchived = sn.getAttribute('data-archived') || '';

                  const sCard = document.createElement('div');
                  // service cards styled light-grey
                  sCard.className = 'border rounded p-2 mb-2 bg-gray-100';
                  const sTop = document.createElement('div');
                  sTop.className = 'flex justify-between items-center';
                  const sTitle = document.createElement('div');
                  sTitle.textContent = sname + ' ( #' + sid + ')';
                  const sButtons = document.createElement('div');
                  sButtons.className = 'flex space-x-2';

                  const sEdit = document.createElement('button');
                  sEdit.type = 'button';
                  sEdit.className = 'px-2 py-1 text-sm bg-yellow-200 rounded';
                  sEdit.textContent = 'Edit';

                  const sDelete = document.createElement('button');
                  sDelete.type = 'button';
                  sDelete.className = 'px-2 py-1 text-sm bg-red-300 rounded';
                  sDelete.textContent = 'Slet';

                  sButtons.appendChild(sEdit);
                  sButtons.appendChild(sDelete);
                  sTop.appendChild(sTitle);
                  sTop.appendChild(sButtons);
                  sCard.appendChild(sTop);

                  const sDetails = document.createElement('div');
                  sDetails.className = 'text-sm text-gray-700 mt-2';
                  // Map type and status to human-friendly labels and format dates as dd-mm-yyyy
                  const typeLabels = {
                    'Billeter': 'Billeter',
                    'Kuponer': 'Kuponer',
                    'Banner': 'Banner',
                    'LogoTrojler': 'Logo på trøjer',
                    'LogoBukser': 'Logo på bukser',
                    'Tickets': 'Tickets',
                    'Coupons': 'Coupons'
                  };
                  const statusLabels = {
                    'AKTIV': 'Aktiv',
                    'IGANG': 'Igang',
                    'UDFORT': 'Udført',
                    'INAKTIV': 'Inaktiv',
                    'true': 'Arkiveret',
                    'false': 'Aktiv'
                  };
                  const displayType = typeLabels[stype] || stype || '';
                  const displayStart = formatDisplayDate(sstart);
                  const displayEnd = formatDisplayDate(send);
                  const displayStatus = statusLabels[String(sarchived)] || sarchived || '';
                  sDetails.innerHTML = `Type: ${escapeHtml(displayType)}<br/>Start: ${escapeHtml(displayStart)}<br/>End: ${escapeHtml(displayEnd)}<br/>Amount/Division: ${escapeHtml(samount)}<br/>Status: ${escapeHtml(displayStatus)}`;
                  sCard.appendChild(sDetails);

                  // small inline edit block for service
                  const sEditBlock = document.createElement('div');
                  sEditBlock.style.display = 'none';
                  sEditBlock.className = 'mt-2';
                  // build basic edit form
                  const sForm = document.createElement('form');
                  sForm.action = '/update/service';
                  sForm.method = 'POST';
                  // hidden id
                  const sIdInput = document.createElement('input'); sIdInput.type = 'hidden'; sIdInput.name = 'id'; sIdInput.value = sid; sForm.appendChild(sIdInput);
                  const serviceContractId = document.createElement("input"); 
                  serviceContractId.type = "hidden"; serviceContractId.name = "contractId"; serviceContractId.value = sn.getAttribute("data-contract-id"); sForm.appendChild(serviceContractId);
                  // name
                  const sNameLab = document.createElement('label'); sNameLab.className = 'block text-sm'; sNameLab.textContent = 'Info/Kommentar'; sForm.appendChild(sNameLab);
                  const sNameInp = document.createElement('input'); sNameInp.name = 'name'; sNameInp.type = 'text'; sNameInp.value = sname; sNameInp.className='border rounded px-2 py-1 w-full'; sForm.appendChild(sNameInp);
                  // type 
                  const sTypeLab = document.createElement('label'); sTypeLab.className='block text-sm'; sTypeLab.textContent='Type'; sForm.appendChild(sTypeLab);
                  const sTypeSel = document.createElement('select'); sTypeSel.name='type'; sTypeSel.className='border rounded px-2 py-1 w-full';
                  // copy options from the Add Service modal so labels/values match
                  const templateType = document.getElementById('addServiceType');
                  if (templateType) {
                    const opts = templateType.querySelectorAll('option');
                    opts.forEach(function(o){ sTypeSel.appendChild(o.cloneNode(true)); });
                    if (stype) sTypeSel.value = stype;
                  }
                  sForm.appendChild(sTypeSel);

                  // amount/division wrapper (we toggle between number input and select for logo types)
                  const sAmtWrapper = document.createElement('div'); sAmtWrapper.id = 's-amount-wrapper-' + sid; sForm.appendChild(sAmtWrapper);
                  function updateSAmountWrapper() {
                    // clear wrapper
                    sAmtWrapper.innerHTML = '';
                    const v = sTypeSel.value;
                    if (v === 'LogoTrojler' || v === 'LogoBukser') {
                      const lab = document.createElement('label'); lab.className='block text-sm'; lab.textContent='Divisionen'; sAmtWrapper.appendChild(lab);
                      const divSel = document.createElement('select'); divSel.name='amountOrDivision'; divSel.className='border rounded px-2 py-1 w-full';
                      // preserve previous numeric value when possible
                      const prev = parseInt(samount) || 1;
                      for (let i=1;i<=10;i++) { const o = document.createElement('option'); o.value = String(i); o.textContent = String(i); if(i===prev) o.selected=true; divSel.appendChild(o); }
                      sAmtWrapper.appendChild(divSel);
                    } else {
                      const lab = document.createElement('label'); lab.className='block text-sm'; lab.textContent='Antal'; sAmtWrapper.appendChild(lab);
                      const inp = document.createElement('input'); inp.name='amountOrDivision'; inp.type='number'; inp.value = (samount || '0'); inp.className='border rounded px-2 py-1 w-full'; sAmtWrapper.appendChild(inp);
                    }
                  }
                  // initialize wrapper based on current type/value
                  updateSAmountWrapper();
                  // update when service type changes
                  sTypeSel.addEventListener('change', updateSAmountWrapper);
                  // start/end/status
                  const sStartLab = document.createElement('label'); sStartLab.className='block text-sm'; sStartLab.textContent='Start Dato'; sForm.appendChild(sStartLab);
                  const sStartInp = document.createElement('input'); sStartInp.name='startDate'; sStartInp.type='date'; sStartInp.value=sstart; sStartInp.className='border rounded px-2 py-1 w-full'; sForm.appendChild(sStartInp);
                  const sEndLab = document.createElement('label'); sEndLab.className='block text-sm'; sEndLab.textContent='Slut Dato'; sForm.appendChild(sEndLab);
                  const sEndInp = document.createElement('input'); sEndInp.name='endDate'; sEndInp.type='date'; sEndInp.value=send; sEndInp.className='border rounded px-2 py-1 w-full'; sForm.appendChild(sEndInp);
                  const sStatusLab = document.createElement('label'); sStatusLab.className='block text-sm'; sStatusLab.textContent='Arkiveret'; sForm.appendChild(sStatusLab);
                  const sStatusSel = document.createElement('select'); sStatusSel.name='archived'; sStatusSel.className='border rounded px-2 py-1 w-full';
                  const sOptTrue = document.createElement('option'); sOptTrue.value = 'true'; sOptTrue.textContent = 'True';
                  const sOptFalse = document.createElement('option'); sOptFalse.value = 'false'; sOptFalse.textContent = 'False';
                  sStatusSel.appendChild(sOptTrue); sStatusSel.appendChild(sOptFalse);
                  if (String(sarchived) === 'true') sStatusSel.value = 'true'; else sStatusSel.value = 'false';
                  sForm.appendChild(sStatusSel);
                  const sBtnRow = document.createElement('div'); sBtnRow.className='flex justify-end space-x-2 mt-2';
                  const sCancel = document.createElement('button'); sCancel.type='button'; sCancel.className='px-3 py-1 bg-gray-200 rounded'; sCancel.textContent='Cancel';
                  const sSave = document.createElement('button'); sSave.type='submit'; sSave.className='px-3 py-1 bg-green-500 text-white rounded'; sSave.textContent='Save';
                  sBtnRow.appendChild(sCancel); sBtnRow.appendChild(sSave); sForm.appendChild(sBtnRow);
                  sEditBlock.appendChild(sForm);
                  sCard.appendChild(sEditBlock);

                  sEdit.addEventListener('click', function(){ sEditBlock.style.display = sEditBlock.style.display==='none' ? 'block' : 'none'; });
                  // cancel should hide the inline edit block
                  sCancel.addEventListener('click', function () { sEditBlock.style.display = 'none'; });
                  sDelete.addEventListener('click', function(){ if(confirm('Slet denne tjeneste?')) postDeleteService(sid, id); });

                  servicesList.appendChild(sCard);
                }
              });
            }
          } catch (e) {
            console.error('populateViewContracts: services render error', e);
          }


          
          // ----------------------------------------------------------
          // REDIGERINGS-FORMULAR (skjult indtil "Edit" klikkes)
          // ----------------------------------------------------------
          const editBlock = document.createElement('div');
          editBlock.id = `editform-${idx}`;
          editBlock.style.display = 'none';
          editBlock.className = 'mt-2 text-sm text-gray-700 border-t pt-2';

          // sender POST til /update/contract
          const form = document.createElement('form');
          form.action = '/update/contract';
          form.method = 'POST';
          form.className = 'space-y-2';
          form.enctype = 'multipart/form-data';

          // skjulte felter: kontrakt-ID + sponsor-navnet (bruges til redirect)
          const hiddenId = document.createElement('input');
          hiddenId.type = 'hidden'; 
          hiddenId.name = 'id'; 
          hiddenId.value = id;
          
          const hiddenSponsorName = document.createElement('input');
          hiddenSponsorName.type = 'hidden'; 
          hiddenSponsorName.name = 'sponsorName'; 
          hiddenSponsorName.value = sponsorDisplayName || '';

          // iføjer så de skulte felter
          form.appendChild(hiddenId);
          form.appendChild(hiddenSponsorName);

          
          // ----------------------------------------------------------
          // Helperfunktion til at tilføje label + input-felt i ét trin
          // ----------------------------------------------------------
          function addLabelAndControl(parent, labelText, control) {
            const lab = document.createElement('label');
            lab.className = 'block text-sm';
            lab.textContent = labelText;
            parent.appendChild(lab);
            parent.appendChild(control);
          }

          // ----------------------------------------------------------
          // Sponsor dropdown (fyldes senere via cloneNode fra template)
          // ----------------------------------------------------------
          const sponsorSelect = document.createElement('select');
          sponsorSelect.id = `sponsor-select-${idx}`;
          sponsorSelect.name = 'sponsorId';
          sponsorSelect.className = 'border rounded px-2 py-1 w-full';
          addLabelAndControl(form, 'Sponsor', sponsorSelect);

          // Inputfelt: kontrakt-navn
          const nameInput = document.createElement('input');
          nameInput.name = 'name'; 
          nameInput.type = 'text'; 
          nameInput.value = name; 
          nameInput.className = 'border rounded px-2 py-1 w-full';
          addLabelAndControl(form, 'Navn', nameInput);

          // Inputfelt: kontrakt-type
          const typeInput = document.createElement('input');
          typeInput.name = 'type'; 
          typeInput.type = 'text'; 
          typeInput.value = type; 
          typeInput.className = 'border rounded px-2 py-1 w-full';
          addLabelAndControl(form, 'Type', typeInput);

          // Startdato-felt
          const startInput = document.createElement('input');
          startInput.name = 'startDate'; 
          startInput.type = 'date'; 
          startInput.value = start || '' ; 
          startInput.className = 'border rounded px-2 py-1 w-full';
          addLabelAndControl(form, 'Start Dato', startInput);

          // Slutdato-felt
          const endInput = document.createElement('input');
          endInput.name = 'endDate'; 
          endInput.type = 'date'; 
          endInput.value = end || '' ; 
          endInput.className = 'border rounded px-2 py-1 w-full';
          addLabelAndControl(form, 'Slut Dato', endInput);

          // Betaling-felt
          const paymentInput = document.createElement('input');
          paymentInput.name = 'payment'; 
          paymentInput.type = 'number'; 
          paymentInput.value = payment; 
          paymentInput.className = 'border rounded px-2 py-1 w-full';
          addLabelAndControl(form, 'Payment', paymentInput);

          // Status-dropdown (true/false)
          const statusSelect = document.createElement('select');
          statusSelect.id = `status-select-${idx}`; 
          statusSelect.name = 'status'; 
          statusSelect.className = 'border rounded px-2 py-1 w-full';
          
          const optTrue = document.createElement('option'); 
          optTrue.value = 'true'; 
          optTrue.textContent = 'True';
          
          const optFalse = document.createElement('option'); 
          optFalse.value = 'false'; 
          optFalse.textContent = 'False';

          // PDF upload field
          const pdfInput = document.createElement('input');
          pdfInput.type = 'file';
          pdfInput.name = 'pdffile';
          pdfInput.accept = 'application/pdf';
          pdfInput.className = 'border rounded px-2 py-1 w-full';
          addLabelAndControl(form, 'Upload PDF', pdfInput);

          statusSelect.appendChild(optTrue); statusSelect.appendChild(optFalse);
          if (String(status) === 'true' || status === true) statusSelect.value = 'true'; else statusSelect.value = 'false';
          addLabelAndControl(form, 'Aktive', statusSelect);


          // ----------------------------------------------------------
          // Knapper nederst (Save og Cancel)
          // ----------------------------------------------------------
          const btnRow = document.createElement('div'); 
          btnRow.className = 'flex justify-end space-x-2 mt-2';
          
          const btnCancel = document.createElement('button'); 
          btnCancel.type = 'button'; 
          btnCancel.className = 'px-3 py-1 bg-gray-200 rounded'; 
          btnCancel.textContent = 'Cancel';

          const btnSave = document.createElement('button'); 
          btnSave.type = 'submit'; 
          btnSave.className = 'px-3 py-1 bg-green-500 text-white rounded'; 
          btnSave.textContent = 'Save';
          btnRow.appendChild(btnCancel); btnRow.appendChild(btnSave);
          form.appendChild(btnRow);

          editBlock.appendChild(form);

          // Assemble container
          container.appendChild(topRow);
          container.appendChild(details);
          // services header + list (if any)
          container.appendChild(servicesWrapper);
          // services button is inline in the header; no floating button required
          container.appendChild(editBlock);

          // Details always shown; no toggle handler required
          // Åbn add-service modal
          btnAddService.addEventListener('click', function () {
            try {
              const modal = document.getElementById('addServiceModal');
              if (!modal) return;
              document.getElementById('addServiceContractId').value = id;
              document.getElementById('addServiceContractName').textContent = name || ('#' + id);
              // reset inputs
              const form = modal.querySelector('form');
              if (form) form.reset();
              modal.style.display = 'flex';
            } catch (e) { console.error('openAddService error', e); }
          });
          // services always visible; no toggle
          // Åbn/luk redigeringsformular
          btnEdit.addEventListener('click', function () {
            editBlock.style.display = editBlock.style.display === 'none' ? 'block' : 'none';
          });
          // Slet kontrakt (kalder back-end via postDeleteContract)
          btnDelete.addEventListener('click', function () {
            if (confirm('Slet denne kontrakt? Dette kan ikke fortrydes.')) {
              postDeleteContract(id, sponsorId);
            }
          });
          // Luk redigeringsformular uden at gemme
          btnCancel.addEventListener('click', function () { editBlock.style.display = 'none'; });

          // Tilføj containeren til modal-listen
          listEl.appendChild(container);

          // udfyld sponsor-dropdown og status-dropdown med rigtige værdier
          // Gør det ved at klone <option>-elementer fra skjult template 
          try {
            const template = document.getElementById('sponsorOptionsTemplate');
            if (template) {
              const sel = document.getElementById('sponsor-select-' + idx);
              if (sel) {
                // clone option nodes from template
                const opts = template.querySelectorAll('option');
                opts.forEach(function (o) {
                  sel.appendChild(o.cloneNode(true));
                });
                // Sæt sponsorId som valgt værdi
                sel.value = n.getAttribute('data-sponsor-id');
              }
            }
            // Sæt korrekt status i status-select
            const statusSel = document.getElementById('status-select-' + idx);
            if (statusSel) statusSel.value = String(status) === 'true' || String(status) === 'True' ? 'true' : 'false';
          } catch (err) {
            // Hvis noget går galt ved udfyldning, log fejl til konsol men fortsæt
            console.error('populateViewContracts: cannot populate selects', err);
          }
        });
      }
      // Sender en POST-anmodning for at slette en kontrakt ved at oprette og indsende en skjult formular.
      // Kaldes fra den dynamisk genererede kontraktkortets knap 'Slet Kontrakt'.
      function postDeleteContract(contractId, sponsorId) {
        try {
          const f = document.createElement('form');
          f.method = 'POST';
          f.action = '/sponsors/deleteContract';
          f.style.display = 'none';
          const inId = document.createElement('input');
          inId.type = 'hidden';
          inId.name = 'contractId';
          inId.value = contractId;
          f.appendChild(inId);
          const inSponsor = document.createElement('input');
          inSponsor.type = 'hidden';
          inSponsor.name = 'sponsorId';
          inSponsor.value = sponsorId || '';
          f.appendChild(inSponsor);
          document.body.appendChild(f);
          f.submit();
        } catch (e) {
          console.error('postDeleteContract error', e);
          alert('Kunne ikke slette kontrakt — se konsol for detaljer.');
        }
      }

      // POST delete service by creating hidden form
      function postDeleteService(serviceId, contractId) {
        try {
          const f = document.createElement('form');
          f.method = 'POST';
          f.action = '/sponsors/deleteService';
          f.style.display = 'none';
          const inId = document.createElement('input');
          inId.type = 'hidden';
          inId.name = 'serviceId';
          inId.value = serviceId;
          f.appendChild(inId);
          document.body.appendChild(f);
          f.submit();
        } catch (e) {
          console.error('postDeleteService error', e);
          alert('Kunne ikke slette tjeneste — se konsol for detaljer.');
        }
      }

      // toggle amount/division input in addService modal
      (function setupServiceModal() {
        const sel = document.getElementById('addServiceType');
        if (!sel) return;
        const wrapper = document.getElementById('amountOrDivisionWrapper');
        function updateWrapper() {
          const v = sel.value;
          wrapper.innerHTML = '';
          if (v === 'LogoTrojler' || v === 'LogoBukser') {
            const lab = document.createElement('label'); lab.className='block text-sm'; lab.textContent='Divisionen'; wrapper.appendChild(lab);
            const divSel = document.createElement('select'); divSel.name='amountOrDivision'; divSel.className='border rounded px-2 py-1 w-full';
            for (let i=1;i<=10;i++) { const o = document.createElement('option'); o.value = String(i); o.textContent = String(i); divSel.appendChild(o); }
            wrapper.appendChild(divSel);
          } else {
            const lab = document.createElement('label'); lab.className='block text-sm'; lab.textContent='Antal'; wrapper.appendChild(lab);
            const inp = document.createElement('input'); inp.name='amountOrDivision'; inp.type='number'; inp.value='0'; inp.className='border rounded px-2 py-1 w-full'; wrapper.appendChild(inp);
          }
        }
        sel.addEventListener('change', updateWrapper);
        // init
        updateWrapper();
      })();

      // basic escaping to avoid injection when rendering attributes
      function escapeHtml(s) {
        if (!s && s !== 0) return '';
        return String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#39;');
      }