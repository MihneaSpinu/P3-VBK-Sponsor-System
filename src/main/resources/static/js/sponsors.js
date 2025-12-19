
// Common sponsor-page helpers: filters, modals, input validation
function toggleSponsorEdit(i) {
	const id = 'sponsorEditForm-' + i;
	const element = document.getElementById(id);
	if (!element) return;
	element.style.display = element.style.display === 'none' || element.style.display === '' ? 'block' : 'none';
}


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


const filter = document.getElementById('sponsorFilter');
if (filter) {
	filter.value = 'active';
	applySponsorFilter();
	filter.addEventListener('change', applySponsorFilter);
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