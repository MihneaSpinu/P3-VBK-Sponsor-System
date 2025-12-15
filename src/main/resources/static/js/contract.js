// Contract rendering and deletion helpers
function populateViewContracts(sponsorId) {
	const listEl = document.getElementById('viewContractsList');
	if (!listEl) return;
	listEl.textContent = '';
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
		const emptyDiv = document.createElement('div');
		emptyDiv.className = 'text-sm text-gray-600';
		emptyDiv.textContent = 'Ingen kontrakter for denne sponsor.';
		listEl.appendChild(emptyDiv);
		return;
	}

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
		const status = n.getAttribute('data-active') || '';
		const pdfFileName = n.getAttribute('data-pdf-filename') || '';

		const container = document.createElement('div');
		container.className = 'border rounded p-2';

		const topRow = document.createElement('div');
		topRow.className = 'flex justify-between items-center';

		const title = document.createElement('div');
		title.className = 'font-medium';
		const nameText = document.createElement('span');
		nameText.textContent = name;
		const idSpan = document.createElement('span');
		idSpan.className = 'text-xs text-gray-500';
		idSpan.textContent = ` (#${id})`;
		title.appendChild(nameText);
		title.appendChild(idSpan);

		const btnGroup = document.createElement('div');
		btnGroup.className = 'flex space-x-2';

		const btnDownload = document.createElement('button');
		btnDownload.type = 'button';
		btnDownload.className = 'px-2 py-1 text-sm bg-green-300 rounded';
		btnDownload.textContent = pdfFileName ? `Download: ${pdfFileName}` : 'Ingen PDF indsat';
		btnDownload.onclick = function () {
			const pdfAvailable = n.getAttribute('data-has-pdf');
			if (pdfAvailable === 'true') {
				window.open(`/getFile/${id}`, '_blank');
			} else {
				alert('Ingen PDF tilgængelig for denne kontrakt.');
			}
		};

		const btnEdit = document.createElement('button');
		btnEdit.type = 'button';
		btnEdit.className = 'px-2 py-1 text-sm bg-yellow-200 rounded';
		btnEdit.textContent = 'Rediger Kontrakten';

		const btnDelete = document.createElement('button');
		btnDelete.type = 'button';
		btnDelete.className = 'px-2 py-1 text-sm bg-red-300 rounded';
		btnDelete.textContent = 'Slet Kontrakt';

		const btnAddService = document.createElement('button');
		btnAddService.type = 'button';
		btnAddService.className = 'px-2 py-1 text-sm bg-blue-500 text-white rounded';
		btnAddService.textContent = 'Tilføj Tjeneste';

		btnGroup.appendChild(btnEdit);
		btnGroup.appendChild(btnDelete);

		topRow.appendChild(title);
		topRow.appendChild(btnGroup);

		const details = document.createElement('div');
		details.id = `details-${idx}`;
		details.style.display = 'block';
		details.className = 'mt-2 text-sm text-gray-700';
		const dType = document.createElement('div'); dType.textContent = `Type: ${type}`;
		const dStart = document.createElement('div'); dStart.textContent = `Start: ${formattedStart}`;
		const dEnd = document.createElement('div'); dEnd.textContent = `End: ${formattedEnd}`;
		const dPayment = document.createElement('div'); dPayment.textContent = `Beløb: ${payment}`;
		let statusString;
		if(status == true || status == 'true') {
			statusString = "Aktiv";
		} else if(status == false || status == 'false') {
			statusString = "Inaktiv";
		}
		const dStatus = document.createElement('div'); dStatus.textContent = `Status: ${statusString}`;
		details.appendChild(dType);
		details.appendChild(dStart);
		details.appendChild(dEnd);
		details.appendChild(dPayment);
		details.appendChild(dStatus);
		details.appendChild(btnDownload);

		const servicesWrapper = document.createElement('div');
		servicesWrapper.className = 'mt-3';
		const servicesHeader = document.createElement('div');
		servicesHeader.className = 'flex items-center justify-between';
		const servicesTitle = document.createElement('div');
		servicesTitle.className = 'text-sm font-medium mb-2';
		servicesTitle.textContent = 'Tjenester';
		servicesHeader.appendChild(servicesTitle);
		btnAddService.className = 'px-3 py-1 text-sm bg-blue-500 text-white rounded';
		servicesHeader.appendChild(btnAddService);
		servicesWrapper.appendChild(servicesHeader);

		const servicesList = document.createElement('div');
		servicesList.className = 'mt-2';
		servicesList.style.display = 'block';
		servicesWrapper.appendChild(servicesList);

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
						const sdivision = sn.getAttribute('data-division') || '';
						const sactive = sn.getAttribute('data-active') || '';

						const sCard = document.createElement('div');
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
						sEdit.textContent = 'Rediger Tjeneste';

						const sDelete = document.createElement('button');
						sDelete.type = 'button';
						sDelete.className = 'px-2 py-1 text-sm bg-red-300 rounded';
						sDelete.textContent = 'Slet Tjeneste';

						sButtons.appendChild(sEdit);
						sButtons.appendChild(sDelete);
						sTop.appendChild(sTitle);
						sTop.appendChild(sButtons);
						sCard.appendChild(sTop);

						const sDetails = document.createElement('div');
						sDetails.className = 'text-sm text-gray-700 mt-2';
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
							'true': 'Ikke opfyldt',
							'false': 'Opfyldt'
						};
						const displayType = typeLabels[stype] || stype || '';
						const displayStart = formatDisplayDate(sstart);
						const displayEnd = formatDisplayDate(send);
						const displayStatus = statusLabels[String(sactive)] || sactive || '';
						sDetails.textContent = '';
						const sLine = function (label, value) { const d = document.createElement('div'); d.textContent = label + ' ' + (value || ''); return d; };
						sDetails.appendChild(sLine('Type:', displayType));
						sDetails.appendChild(sLine('Start:', displayStart));
						sDetails.appendChild(sLine('End:', displayEnd));
						if (stype === 'LogoTrojler' || stype === 'LogoBukser') {
							sDetails.appendChild(sLine('Division:', sdivision));
						} else {
							sDetails.appendChild(sLine('Antal:', samount));
						}
						sDetails.appendChild(sLine('Status:', displayStatus));
						sCard.appendChild(sDetails);

						const sEditBlock = document.createElement('div');
						sEditBlock.style.display = 'none';
						sEditBlock.className = 'mt-2';
						const sForm = document.createElement('form');
						sForm.action = '/update/service';
						sForm.method = 'POST';
						const sIdInput = document.createElement('input'); sIdInput.type = 'hidden'; sIdInput.name = 'id'; sIdInput.value = sid; sForm.appendChild(sIdInput);
						const serviceContractId = document.createElement('input'); serviceContractId.type = 'hidden'; serviceContractId.name = 'contractId'; serviceContractId.value = sn.getAttribute('data-contract-id'); sForm.appendChild(serviceContractId);
						const sNameLab = document.createElement('label'); sNameLab.className = 'block text-sm'; sNameLab.textContent = 'Info/Kommentar'; sForm.appendChild(sNameLab);
						const sNameInp = document.createElement('input'); sNameInp.name = 'name'; sNameInp.type = 'text'; sNameInp.value = sname; sNameInp.className='border rounded px-2 py-1 w-full'; sForm.appendChild(sNameInp);

						const sTypeLab = document.createElement('label'); sTypeLab.className='block text-sm'; sTypeLab.textContent='Type'; sForm.appendChild(sTypeLab);
						const sTypeSel = document.createElement('select'); sTypeSel.name='type'; sTypeSel.className='border rounded px-2 py-1 w-full';
						const templateType = document.getElementById('addServiceType');
						if (templateType) {
							const opts = templateType.querySelectorAll('option');
							opts.forEach(function(o){ sTypeSel.appendChild(o.cloneNode(true)); });
							if (stype) sTypeSel.value = stype;
						}
						sForm.appendChild(sTypeSel);

						const sAmtWrapper = document.createElement('div'); sAmtWrapper.id = 's-amount-wrapper-' + sid; sForm.appendChild(sAmtWrapper);
						function updateSAmountWrapper() {
							sAmtWrapper.textContent = '';
							const v = sTypeSel.value;
								if (v === 'LogoTrojler' || v === 'LogoBukser') {
								const lab = document.createElement('label'); lab.className='block text-sm'; lab.textContent='Divisionen'; sAmtWrapper.appendChild(lab);
								const divSel = document.createElement('select'); divSel.name='division'; divSel.className='border rounded px-2 py-1 w-full';
								const prev = parseInt(sdivision) || 1;
								for (let i=1;i<=10;i++) { const o = document.createElement('option'); o.value = String(i); o.textContent = String(i); if(i===prev) o.selected=true; divSel.appendChild(o); }
								sAmtWrapper.appendChild(divSel);
							} else {
								const lab = document.createElement('label'); lab.className='block text-sm'; lab.textContent='Antal'; sAmtWrapper.appendChild(lab);
								const inp = document.createElement('input'); inp.name='amount'; inp.type='number'; inp.value = (samount || '0'); inp.className='border rounded px-2 py-1 w-full'; sAmtWrapper.appendChild(inp);
							}
						}
						updateSAmountWrapper();
						sTypeSel.addEventListener('change', updateSAmountWrapper);

						const sStartLab = document.createElement('label'); sStartLab.className='block text-sm'; sStartLab.textContent='Start Dato'; sForm.appendChild(sStartLab);
						const sStartInp = document.createElement('input'); sStartInp.name='startDate'; sStartInp.type='date'; sStartInp.value=sstart; sStartInp.className='border rounded px-2 py-1 w-full'; sForm.appendChild(sStartInp);
						const sEndLab = document.createElement('label'); sEndLab.className='block text-sm'; sEndLab.textContent='Slut Dato'; sForm.appendChild(sEndLab);
						const sEndInp = document.createElement('input'); sEndInp.name='endDate'; sEndInp.type='date'; sEndInp.value=send; sEndInp.className='border rounded px-2 py-1 w-full'; sForm.appendChild(sEndInp);
						const sStatusLab = document.createElement('label'); sStatusLab.className='block text-sm'; sStatusLab.textContent='Status'; sForm.appendChild(sStatusLab);
						const sStatusSel = document.createElement('select'); sStatusSel.name='active'; sStatusSel.className='border rounded px-2 py-1 w-full';
						const sOptTrue = document.createElement('option'); sOptTrue.value = 'true'; sOptTrue.textContent = 'Ikke opfyldt';
						const sOptFalse = document.createElement('option'); sOptFalse.value = 'false'; sOptFalse.textContent = 'Opfyldt';
						sStatusSel.appendChild(sOptTrue); sStatusSel.appendChild(sOptFalse); 
						if (String(sactive) === 'true') sStatusSel.value = 'true'; else sStatusSel.value = 'false';
						sForm.appendChild(sStatusSel);
						const sBtnRow = document.createElement('div'); sBtnRow.className='flex justify-end space-x-2 mt-2';
						const sCancel = document.createElement('button'); sCancel.type='button'; sCancel.className='px-3 py-1 bg-gray-200 rounded'; sCancel.textContent='Cancel';
						const sSave = document.createElement('button'); sSave.type='submit'; sSave.className='px-3 py-1 bg-green-500 text-white rounded'; sSave.textContent='Save';
						sBtnRow.appendChild(sCancel); sBtnRow.appendChild(sSave); sForm.appendChild(sBtnRow);
						sEditBlock.appendChild(sForm);
						sCard.appendChild(sEditBlock);

						sEdit.addEventListener('click', function(){ sEditBlock.style.display = sEditBlock.style.display==='none' ? 'block' : 'none'; });
						sCancel.addEventListener('click', function () { sEditBlock.style.display = 'none'; });
						sDelete.addEventListener('click', function(){ if(confirm('Slet denne tjeneste?')) postDeleteService(sid, id); });

						servicesList.appendChild(sCard);
					}
				});
			}
		} catch (e) {
			console.error('populateViewContracts: services render error', e);
		}

		const editBlock = document.createElement('div');
		editBlock.id = `editform-${idx}`;
		editBlock.style.display = 'none';
		editBlock.className = 'mt-2 text-sm text-gray-700 border-t pt-2';

		const form = document.createElement('form');
		form.action = '/update/contract';
		form.method = 'POST';
		form.className = 'space-y-2';
		form.enctype = 'multipart/form-data';

		const hiddenId = document.createElement('input'); hiddenId.type = 'hidden'; hiddenId.name = 'id'; hiddenId.value = id;
		const hiddenSponsorName = document.createElement('input'); hiddenSponsorName.type = 'hidden'; hiddenSponsorName.name = 'sponsorName'; hiddenSponsorName.value = (document.getElementById('viewModalSponsorName')||{}).textContent || '';
		form.appendChild(hiddenId); form.appendChild(hiddenSponsorName);

		function addLabelAndControl(parent, labelText, control) {
			const lab = document.createElement('label'); lab.className = 'block text-sm'; lab.textContent = labelText; parent.appendChild(lab); parent.appendChild(control);
		}

		const sponsorSelect = document.createElement('select'); sponsorSelect.id = `sponsor-select-${idx}`; sponsorSelect.name = 'sponsorId'; sponsorSelect.className = 'border rounded px-2 py-1 w-full'; addLabelAndControl(form, 'Sponsor', sponsorSelect);

		const nameInput = document.createElement('input'); nameInput.name = 'name'; nameInput.type = 'text'; nameInput.value = name; nameInput.className = 'border rounded px-2 py-1 w-full'; addLabelAndControl(form, 'Navn', nameInput);
		nameInput.required = true;

		const typeInput = document.createElement('input'); typeInput.name = 'type'; typeInput.type = 'text'; typeInput.value = type; typeInput.className = 'border rounded px-2 py-1 w-full'; addLabelAndControl(form, 'Type', typeInput);

		const startInput = document.createElement('input'); startInput.name = 'startDate'; startInput.type = 'date'; startInput.value = start || '' ; startInput.className = 'border rounded px-2 py-1 w-full'; addLabelAndControl(form, 'Start Dato', startInput);

		const endInput = document.createElement('input'); endInput.name = 'endDate'; endInput.type = 'date'; endInput.value = end || '' ; endInput.className = 'border rounded px-2 py-1 w-full'; addLabelAndControl(form, 'Slut Dato', endInput);

		const paymentInput = document.createElement('input'); paymentInput.name = 'payment'; paymentInput.type = 'number'; paymentInput.value = payment; paymentInput.className = 'border rounded px-2 py-1 w-full'; addLabelAndControl(form, 'Beløb', paymentInput);
		
		
		const statusSelect = document.createElement('select'); statusSelect.id = `status-select-${idx}`; statusSelect.name = 'status'; statusSelect.className = 'border rounded px-2 py-1 w-full';
		const optTrue = document.createElement('option'); optTrue.value = 'true'; optTrue.textContent = 'True';
		const optFalse = document.createElement('option'); optFalse.value = 'false'; optFalse.textContent = 'False';
		
		const pdfInput = document.createElement('input'); pdfInput.type = 'file'; pdfInput.name = 'pdffile'; pdfInput.accept = 'application/pdf'; pdfInput.className = 'border rounded px-2 py-1 w-full'; addLabelAndControl(form, 'Upload PDF', pdfInput);

		/*
		statusSelect.appendChild(optTrue); statusSelect.appendChild(optFalse);
		if (String(status) === 'true' || status === true) statusSelect.value = 'true'; else statusSelect.value = 'false';
		addLabelAndControl(form, 'Aktiv', statusSelect);
		*/
		const btnRow = document.createElement('div'); btnRow.className = 'flex justify-end space-x-2 mt-2';
		const btnCancel = document.createElement('button'); btnCancel.type = 'button'; btnCancel.className = 'px-3 py-1 bg-gray-200 rounded'; btnCancel.textContent = 'Cancel';
		const btnSave = document.createElement('button'); btnSave.type = 'submit'; btnSave.className = 'px-3 py-1 bg-green-500 text-white rounded'; btnSave.textContent = 'Save';
		btnRow.appendChild(btnCancel); btnRow.appendChild(btnSave); form.appendChild(btnRow);
		editBlock.appendChild(form);

		container.appendChild(topRow);
		container.appendChild(details);
		container.appendChild(servicesWrapper);
		container.appendChild(editBlock);

		btnAddService.addEventListener('click', function () {
			try {
				const modal = document.getElementById('addServiceModal');
				if (!modal) return;
				document.getElementById('addServiceContractId').value = id;
				document.getElementById('addServiceContractName').textContent = name || ('#' + id);
				const f = modal.querySelector('form'); if (f) f.reset();
					function ensureAddWrapper() {
					const sel = document.getElementById('addServiceType');
					const wrapper = document.getElementById('amountOrDivisionWrapper');
					if (!sel || !wrapper) return;
					function render() {
						wrapper.textContent = '';
						if (sel.value === 'LogoTrojler' || sel.value === 'LogoBukser') {
							const lab = document.createElement('label'); lab.className='block text-sm'; lab.textContent='Divisionen'; wrapper.appendChild(lab);
							const divSel = document.createElement('select'); divSel.name='division'; divSel.className='border rounded px-2 py-1 w-full';
							for (let i=1;i<=10;i++) { const o = document.createElement('option'); o.value = String(i); o.textContent = String(i); divSel.appendChild(o); }
							wrapper.appendChild(divSel);
						} else {
							const lab = document.createElement('label'); lab.className='block text-sm'; lab.textContent='Antal'; wrapper.appendChild(lab);
							const inp = document.createElement('input'); inp.name='amount'; inp.type='number'; inp.value='0'; inp.className='border rounded px-2 py-1 w-full'; wrapper.appendChild(inp);
						}
					}
					sel.removeEventListener && sel.removeEventListener('change', render);
					sel.addEventListener('change', render);
					render();
				}
				ensureAddWrapper();
				modal.style.display = 'flex';
			} catch (e) { console.error('openAddService error', e); }
		});

		btnEdit.addEventListener('click', function () { editBlock.style.display = editBlock.style.display === 'none' ? 'block' : 'none'; });
		btnDelete.addEventListener('click', function () { if (confirm('Slet denne kontrakt? Dette kan ikke fortrydes.')) { postDeleteContract(id, sponsorId); } });
		btnCancel.addEventListener('click', function () { editBlock.style.display = 'none'; });

		listEl.appendChild(container);

		try {
			const template = document.getElementById('sponsorOptionsTemplate');
			if (template) {
				const sel = document.getElementById('sponsor-select-' + idx);
				if (sel) {
					const opts = template.querySelectorAll('option');
					opts.forEach(function (o) { sel.appendChild(o.cloneNode(true)); });
					sel.value = n.getAttribute('data-sponsor-id');
				}
			}
			const statusSel = document.getElementById('status-select-' + idx);
			if (statusSel) statusSel.value = String(status) === 'true' || String(status) === 'True' ? 'Aktiv' : 'Inaktiv';
		} catch (err) {
			console.error('populateViewContracts: cannot populate selects', err);
		}
	});
};

function postDeleteContract(contractId, sponsorId) {
	try {
		const f = document.createElement('form');
		f.method = 'POST';
		f.action = '/sponsors/deleteContract';
		f.style.display = 'none';
		const inId = document.createElement('input'); inId.type = 'hidden'; inId.name = 'contractId'; inId.value = contractId; f.appendChild(inId);
		const inSponsor = document.createElement('input'); inSponsor.type = 'hidden'; inSponsor.name = 'sponsorId'; inSponsor.value = sponsorId || ''; f.appendChild(inSponsor);
		document.body.appendChild(f);
		f.submit();
	} catch (e) {
		console.error('postDeleteContract error', e);
		alert('Kunne ikke slette kontrakt — se konsol for detaljer.');
	}
}

// Optional helper: toggle a contract edit form by index (keeps parity with previous inline code)
function toggleContractEdit(i) {
	const id = 'contractEditForm-' + i;
	const el = document.getElementById(id);
	if (!el) return;
	el.style.display = el.style.display === 'none' || el.style.display === '' ? 'block' : 'none';
}

