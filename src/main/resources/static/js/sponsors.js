document.addEventListener('DOMContentLoaded', function () {
  try {
    const modal = document.getElementById('Smodal');
    if (!modal) return;
    const modalContent = modal.querySelector('.SponsorModal');
    const closeButtons = modal.querySelectorAll('.CloseModal');

    document.querySelectorAll('.sponsor-open-btn').forEach(button => {
      button.addEventListener('click', function () {
        const sponsorId = button.getAttribute('data-sponsor-id');
        const template = document.getElementById('sponsor-modal-' + sponsorId);
        if (!template) return;

        // Clear existing content safely and clone template children
        if (modalContent) modalContent.textContent = '';
        Array.from(template.children).forEach(child => { 
			if (modalContent) modalContent.appendChild(child.cloneNode(true)); 
		});

        modal.style.display = 'flex';
      });
    });

    closeButtons.forEach(button => {
      button.addEventListener('click', event => {
        event.preventDefault();
        if (modalContent) modalContent.textContent = '';
        modal.style.display = 'none';
      });
    });
  } catch (error) {
    console.error(error);
  }
});
// Common sponsor-page helpers: filters, modals, input validation
function toggleSponsorEdit(i) {
	const id = 'sponsorEditForm-' + i;
	const element = document.getElementById(id);
	if (!element) return;
	element.style.display = element.style.display === 'none' || element.style.display === '' ? 'block' : 'none';
}

function closeModal(id) {
	const element = document.getElementById(id);
	if (!element) return;
	element.style.display = 'none';
}

function openAddModal(button) {
	try {
		const sponsorId = button.getAttribute('data-sponsor-id');
		const sponsorName = button.getAttribute('data-sponsor-name') || '';
		const idElement = document.getElementById('addModalSponsorId');
		const nameElement = document.getElementById('addModalSponsorName');
		if (idElement) idElement.value = sponsorId;
		if (nameElement) nameElement.textContent = sponsorName;
		const modal = document.getElementById('addContractModal');
		if (modal) modal.style.display = 'flex';
	} catch (error) {
		console.error(error);
	}
}

function openViewModal(buttonElement) {
	try {
		const sponsorId = buttonElement.getAttribute('data-sponsor-id');
		const sponsorName = buttonElement.getAttribute('data-sponsor-name') || '';
		const nameElement = document.getElementById('viewModalSponsorName');
		if (nameElement) nameElement.textContent = sponsorName || ('#' + sponsorId);
		if (typeof populateViewContracts === 'function') populateViewContracts(sponsorId);
		const modal = document.getElementById('viewContractsModal');
		if (modal) modal.style.display = 'flex';
	} catch (error) {
		console.error(error);
	}
}


(function () {


	// filter function
	function applySponsorFilter() {
		const select = document.getElementById('sponsorFilter');
		if (!select) return;
		const value = select.value;
		document.querySelectorAll('.sponsor-item').forEach(element => {
			const status = element.getAttribute('data-active');
			if (value === 'all') 			element.style.display = '';
			else if (value === 'active') 	element.style.display = status === 'true' || status === 'True' ? '' : 'none';
			else if (value === 'inactive') 	element.style.display = status === 'false' || status === 'False' ? '' : 'none';
		});
	}

	// init on DOMContentLoaded
	document.addEventListener('DOMContentLoaded', () => {
		const filter = document.getElementById('sponsorFilter');
		if (filter) {
			filter.value = 'active';
			applySponsorFilter();
			filter.addEventListener('change', applySponsorFilter);
		}
	});
})();

    let statusEditing = { serviceId: null, active: null, element: null };
    let selectedActive = true;

    function openStatusModal(el, evt) {
      console.log('openStatusModal called', el, evt);
      try { 
		if (evt && evt.stopPropagation) evt.stopPropagation(); 
	} catch (err) { 
		console.warn('stopPropagation failed', err); 
	}

      const id = el && el.getAttribute ? el.getAttribute('data-service-id') : null;
      const name = el && el.getAttribute ? el.getAttribute('data-service-name') || ('Service ' + id) : ('Service ' + id);
      const activeAttr = el && el.getAttribute ? el.getAttribute('data-active') : null;
      const active = activeAttr === 'true' || activeAttr === '1';

      statusEditing = { serviceId: id, active: active, element: el };
      document.getElementById('statusServiceName').textContent = name;

 
      setSelectedStatus(active);
      document.getElementById('statusModal').classList.remove('hidden');
    }

    function closeStatusModal() {
      document.getElementById('statusModal').classList.add('hidden');
      statusEditing = { serviceId: null, active: null, element: null };
    }

    function setSelectedStatus(active) {
      selectedActive = active;
      const aBtn = document.getElementById('setActiveBtn');
      const iBtn = document.getElementById('setInactiveBtn');
      if (active) {
        aBtn.classList.add('ring', 'ring-2', 'ring-red-300');
        aBtn.classList.remove('opacity-60');
        iBtn.classList.remove('ring', 'ring-2', 'ring-green-300');
        iBtn.classList.add('opacity-60');
      } else {
        iBtn.classList.add('ring', 'ring-2', 'ring-green-300');
        iBtn.classList.remove('opacity-60');
        aBtn.classList.remove('ring', 'ring-2', 'ring-red-300');
        aBtn.classList.add('opacity-60');
      }
    }

document.addEventListener('DOMContentLoaded', () => {
	const a = document.getElementById('setActiveBtn');
	const i = document.getElementById('setInactiveBtn');
	const save = document.getElementById('statusSaveBtn');

	a && a.addEventListener('click', () => setSelectedStatus(true));
	i && i.addEventListener('click', () => setSelectedStatus(false));

	save && save.addEventListener('click', () => {
		if (!statusEditing.serviceId) return closeStatusModal();
		const id = statusEditing.serviceId;
		const active = selectedActive;

		const body = new URLSearchParams();
		body.append('serviceId', id);
		body.append('active', active.toString());

		fetch('/sponsors/setServiceArchived', {
			method: 'POST',
			headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
			body: body.toString(),
			credentials: 'same-origin'
		})
		.then(res => {
		if (!res.ok) throw new Error('Server responded ' + res.status);
		return res.text();
		})
		.then(() => {
			const els = document.querySelectorAll('[data-service-id]');
			els.forEach(el => {
			if (String(el.getAttribute('data-service-id')) === String(id)) {
				el.textContent = active ? 'Ikke opfyldt' : 'Opfyldt';


				el.setAttribute('data-active', active ? 'true' : 'false'); 

				el.classList.remove('bg-red-400', 'bg-green-400');
				el.classList.add(active ? 'bg-red-400' : 'bg-green-400');
			}
			});
			closeStatusModal();
		})
		.catch(error => {
		console.error(error);
		alert('Kunne ikke opdatere status på serveren. Prøv igen.');
		});
	});

	document.addEventListener('click', e => {
	const btn = e.target.closest && e.target.closest('.status-btn');
	if (!btn) return;
	console.log('homepage: status-btn clicked', btn.getAttribute('data-service-id'));
	e.stopPropagation();
	openStatusModal(btn, e);
	});
});