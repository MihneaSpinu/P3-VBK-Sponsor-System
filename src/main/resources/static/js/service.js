// Service-related helpers used by sponsors/contract pages
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
		// include contractId for redirect/context if provided
		if (contractId !== undefined) {
			const inContract = document.createElement('input');
			inContract.type = 'hidden';
			inContract.name = 'contractId';
			inContract.value = contractId;
			f.appendChild(inContract);
		}
		document.body.appendChild(f);
		f.submit();
	} catch (e) {
		console.error('postDeleteService error', e);
		alert('Kunne ikke slette tjeneste — se konsol for detaljer.');
	}
}

// Setup behavior for the Add Service modal (toggle amount/division input)
(function setupServiceModal() {
	function updateWrapper(sel, wrapper) {
		const v = sel.value;
		wrapper.textContent = '';
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

    btnAddService.addEventListener('click', function () {
			try {
				const modal = document.getElementById('addServiceModal');
				if (!modal) return;
				document.getElementById('addServiceContractId').value = id;
				document.getElementById('addServiceContractName').textContent = name || ('#' + id);
				const f = modal.querySelector('form'); if (f) f.reset();
				modal.style.display = 'flex';
			} catch (e) { console.error('openAddService error', e); }
		});

    
		const sel = document.getElementById('addServiceType');
		if (!sel) return;
		const wrapper = document.getElementById('amountOrDivisionWrapper');
		if (!wrapper) return;
		function update() { updateWrapper(sel, wrapper); }
		sel.addEventListener('change', update);
		update();
})();

