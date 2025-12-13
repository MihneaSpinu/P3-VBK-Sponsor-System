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
		try { console.debug('service.js:updateWrapper selected value ->', v); } catch (e) {}
		wrapper.textContent = '';
		if (v === 'LogoTrojler' || v === 'LogoBukser') {
			try { console.debug('service.js:rendering Division select for', v); } catch (e) {}
			const lab = document.createElement('label'); lab.className='block text-sm'; lab.textContent='Divisionen'; wrapper.appendChild(lab);
			const divSel = document.createElement('select'); divSel.name='division'; divSel.className='border rounded px-2 py-1 w-full';
			for (let i=1;i<=10;i++) { const o = document.createElement('option'); o.value = String(i); o.textContent = String(i); divSel.appendChild(o); }
			wrapper.appendChild(divSel);
		} else {
			const lab = document.createElement('label'); lab.className='block text-sm'; lab.textContent='Antal'; wrapper.appendChild(lab);
			const inp = document.createElement('input'); inp.name='amount'; inp.type='number'; inp.value='0'; inp.className='border rounded px-2 py-1 w-full'; wrapper.appendChild(inp);
		}
	}

	const sel = document.getElementById('addServiceType');
	const wrapper = document.getElementById('amountOrDivisionWrapper');
	if (sel && wrapper) {
		function update() { updateWrapper(sel, wrapper); }
		sel.addEventListener('change', update);
		update();
	}
	window.updateAddServiceWrapper = function () {
		const s = document.getElementById('addServiceType');
		const w = document.getElementById('amountOrDivisionWrapper');
		if (!s || !w) return;
		updateWrapper(s, w);
	};
})();

