  try {
    const modal = document.getElementById('Smodal');
    const modalContent = modal.querySelector('.SponsorModal');
    const closeButtons = modal.querySelectorAll('.CloseModal');

    document.querySelectorAll('.sponsor-open-btn').forEach(button => {
      button.addEventListener('click', () => {
        const sponsorId = button.getAttribute('data-sponsor-id');
        const template = document.getElementById('sponsor-modal-' + sponsorId);

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


let statusEditing = { serviceId: null, active: null, element: null };
let selectedActive = true;

function openStatusModal(element, event) {
	try { 
		if (event && event.stopPropagation) event.stopPropagation(); 
	} catch (err) { 
		console.warn('stopPropagation failed', err); 
	}

	const id = element.getAttribute ? element.getAttribute('data-service-id') : null;
	const name = element.getAttribute ? element.getAttribute('data-service-name') || ('Service ' + id) : ('Service ' + id);
	const activeAttr = element.getAttribute ? element.getAttribute('data-active') : null;
	const active = activeAttr === 'true' || activeAttr === '1';

	statusEditing = { serviceId: id, active: active, element: element };
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
	const activeButton = document.getElementById('setActiveBtn');
	const inactiveButton = document.getElementById('setInactiveBtn');
	if (active) {
		activeButton.classList.add('ring', 'ring-2', 'ring-red-300');
		activeButton.classList.remove('opacity-60');
		inactiveButton.classList.remove('ring', 'ring-2', 'ring-green-300');
		inactiveButton.classList.add('opacity-60');
	} else {
		inactiveButton.classList.add('ring', 'ring-2', 'ring-green-300');
		inactiveButton.classList.remove('opacity-60');
		activeButton.classList.remove('ring', 'ring-2', 'ring-red-300');
		activeButton.classList.add('opacity-60');
	}
}


const activeButton = document.getElementById('setActiveBtn');
const inactiveButton = document.getElementById('setInactiveBtn');
const saveButton = document.getElementById('statusSaveBtn');

activeButton.addEventListener('click', () => setSelectedStatus(true));
inactiveButton.addEventListener('click', () => setSelectedStatus(false));

saveButton.addEventListener('click', () => {
	if (!statusEditing.serviceId) return closeStatusModal();
	const id = statusEditing.serviceId;
	const active = selectedActive;

	const body = new FormData();
	body.append('serviceId', id);
	body.append('active', active.toString());

	fetch('/sponsors/setServiceArchived', {
		method: 'POST',
		body: body
	})
	.then(response => {
	if (!response.ok) throw new Error('Server responded ' + response.status);
		return response.text();
	})
	.then(() => {
		const elements = document.querySelectorAll('[data-service-id]');
		elements.forEach(element => {
		if (String(element.getAttribute('data-service-id')) === String(id)) {
			element.textContent = active ? 'Ikke opfyldt' : 'Opfyldt';
			element.setAttribute('data-active', active ? 'true' : 'false'); 
			element.classList.remove('bg-red-400', 'bg-green-400');
			element.classList.add(active ? 'bg-red-400' : 'bg-green-400');
		}
		});
		closeStatusModal();
	})
	.catch(error => {
		console.error(error);
		alert('Kunne ikke opdatere status på serveren. Prøv igen.');
	});
});

document.addEventListener('click', event => {
	const button = event.target.closest && event.target.closest('.status-btn');
	if (!button) return;
	event.stopPropagation();
	openStatusModal(button, event);
});
