
function postDeleteService(serviceId, contractId) {
	try {
		const form 			= 	document.createElement('form');
		form.method 		= 	'POST';
		form.action 		= 	'/api/service/delete';
		form.style.display 	= 	'none';
		const inputId 		= 	document.createElement('input');
		inputId.type 		= 	'hidden';
		inputId.name 		= 	'serviceId';
		inputId.value 		= 	serviceId;
		form.appendChild(inputId);
		if (contractId !== undefined) {
			const inputContract = 	document.createElement('input');
			inputContract.type 	= 	'hidden';
			inputContract.name 	= 	'contractId';
			inputContract.value = 	contractId;
			form.appendChild(inputContract);
		}
		document.body.appendChild(form);
		form.submit();
	} catch (error) {
		console.error(error);
	}
}

function setupServiceModal() {
	function updateWrapper(select, wrapper) {
		const value = select.value;
		wrapper.textContent = '';
		if (value === 'LogoTrojer' || value === 'LogoBukser') {
			const label = document.createElement('label'); 
			label.className='block text-sm'; 
			label.textContent='Divisionen'; 
			wrapper.appendChild(label);
			const divSelect = document.createElement('select'); 
			divSelect.name='division'; 
			divSelect.className='border rounded px-2 py-1 w-full';
			for (let i = 1; i <= 10; i++) { 
				const option = document.createElement('option'); 
				option.value = String(i); 
				option.textContent = String(i); 
				divSelect.appendChild(option); 
			}
			wrapper.appendChild(divSelect);
		} else {
			const label = document.createElement('label');
			label.className='block text-sm'; 
			label.textContent='Antal'; 
			wrapper.appendChild(label);

			const input = document.createElement('input'); 
			input.name='amount'; 
			input.type='number'; 
			input.value='0'; 
			input.className='border rounded px-2 py-1 w-full'; 
			wrapper.appendChild(input);
		}
	}

	const select = document.getElementById('addServiceType');
	const wrapper = document.getElementById('amountOrDivisionWrapper');
	if (select && wrapper) {
		select.addEventListener('change', updateWrapper(select, wrapper));
		updateWrapper(select, wrapper)
	}
};

