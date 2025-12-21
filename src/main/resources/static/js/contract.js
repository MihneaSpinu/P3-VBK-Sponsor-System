function populateViewContracts(sponsorId) {

	const contractList = document.getElementById("viewContractsList");
	const container = document.getElementById("contractsData");
	if (!contractList) return;
	if (!container) return;
	contractList.textContent = '';

	const hiddenContractData = container.querySelectorAll(".contract-data");
	const matches = [];
	hiddenContractData.forEach(field => {
		if (String(field.getAttribute("data-sponsor-id")) === String(sponsorId)) {
			matches.push(field);
		}
	});

	if (matches.length === 0) {
		const emptyDiv = document.createElement('div');
		emptyDiv.className = 'text-sm text-gray-600';
		emptyDiv.textContent = "Ingen kontrakter for denne sponsor";
		contractList.appendChild(emptyDiv);
		return;
	}

	function formatDisplayDate(date) {

		if (!date && date !== 0) return "";
		const dateString = String(date);
		const formattedDate = dateString.match(/^(\d{4})-(\d{2})-(\d{2})/); // 4 tegn for år, 2 tegn for måned, 2 tegn for dag

		if (formattedDate) return `${formattedDate[3]}-${formattedDate[2]}-${formattedDate[1]}`;
		else return "";
	}


	matches.forEach((field, index) => {
		const id 				= 	field.getAttribute('data-id');
		const name 				= 	field.getAttribute('data-name') 		|| "";
		const type 				= 	field.getAttribute('data-type') 		|| "";
		const start 			= 	field.getAttribute('data-start') 		|| "";
		const end 				= 	field.getAttribute('data-end') 			|| "";
		const formattedStart 	= 	formatDisplayDate(start);
		const formattedEnd 		= 	formatDisplayDate(end);
		const payment 			= 	field.getAttribute('data-payment') 		|| "";
		const status 			= 	field.getAttribute('data-active') 		|| "";
		const pdfFileName 		= 	field.getAttribute('data-pdf-filename') || "";

		const container = document.createElement('div');
		container.className = 'border rounded p-2';

		const topRow = document.createElement('div');
		topRow.className = 'flex justify-between items-center';

		const title = document.createElement('div');
		title.className = 'font-medium';

		const nameText = document.createElement('span');
		nameText.textContent = name;

		title.appendChild(nameText);

		const buttonContainer = document.createElement('div');
		buttonContainer.className = 'flex space-x-2';

		const downloadButton = document.createElement('button');
		downloadButton.type = 'button';
		downloadButton.className = 'px-2 py-1 text-sm bg-green-300 rounded';
		downloadButton.textContent = pdfFileName ? `Download: ${pdfFileName}` : 'Ingen PDF indsat';
		downloadButton.onclick = () => {
			const pdfAvailable = field.getAttribute('data-has-pdf');
			if (pdfAvailable === 'true') {
				window.open(`/api/contract/getFile/${id}`, '_blank'); // jank
			} else {
				alert('Ingen PDF tilgængelig for denne kontrakt.');
			}
		};

		const editButton = document.createElement('button');
		editButton.type = 'button';
		editButton.className = 'px-2 py-1 text-sm bg-yellow-200 rounded';
		editButton.textContent = 'Rediger Kontrakt';

		const deleteButton = document.createElement('button');
		deleteButton.type = 'button';
		deleteButton.className = 'px-2 py-1 text-sm bg-red-300 rounded';
		deleteButton.textContent = 'Slet Kontrakt';

		const addServiceButton = document.createElement('button');
		addServiceButton.type = 'button';
		addServiceButton.className = 'px-3 py-1 text-sm bg-blue-500 text-white rounded';
		addServiceButton.textContent = 'Tilføj Tjeneste';

		buttonContainer.appendChild(editButton);
		buttonContainer.appendChild(deleteButton);

		topRow.appendChild(title);
		topRow.appendChild(buttonContainer);

		const contractDetails = document.createElement('div');
		contractDetails.id = `details-${index}`;
		contractDetails.style.display = 'block';
		contractDetails.className = 'mt-2 text-sm text-gray-700';

		const contractType = document.createElement('div'); 	
		contractType.textContent = `Type: ${type}`;

		const contractStart = document.createElement('div'); 	
		contractStart.textContent = `Start: ${formattedStart}`;

		const contractEnd = document.createElement('div'); 		
		contractEnd.textContent = `Slut: ${formattedEnd}`;

		const contractPayment = document.createElement('div'); 	
		contractPayment.textContent = `Beløb: ${payment}`;

		const contractStatus = document.createElement('div'); 	
		contractStatus.textContent 	= "Status: " + (status === "true" ? "Aktiv" : "Inaktiv");

		contractDetails.appendChild(contractType);
		contractDetails.appendChild(contractStart);
		contractDetails.appendChild(contractEnd);
		contractDetails.appendChild(contractPayment);
		contractDetails.appendChild(contractStatus);
		contractDetails.appendChild(downloadButton);

		const servicesContainer = document.createElement('div');
		servicesContainer.className = 'mt-3';

		const servicesHeader = document.createElement('div');
		servicesHeader.className = 'flex items-center justify-between';

		const servicesTitle = document.createElement('div');
		servicesTitle.className = 'text-sm font-medium mb-2';
		servicesTitle.textContent = 'Tjenester';
		servicesHeader.appendChild(servicesTitle);
		servicesHeader.appendChild(addServiceButton);
		servicesContainer.appendChild(servicesHeader);

		const servicesList = document.createElement('div');
		servicesList.className = 'mt-2';
		servicesList.style.display = 'block';
		servicesContainer.appendChild(servicesList);

		try {
			const servicesData = document.getElementById('servicesData');
			if (servicesData) {
				const serviceFields = servicesData.querySelectorAll('.service-data');
				serviceFields.forEach(field => {
					if (String(field.getAttribute('data-contract-id')) === String(id)) {
						const serviceId 		= 	field.getAttribute('data-id');
						const serviceName 		= 	field.getAttribute('data-name') || '';
						const serviceType 		= 	field.getAttribute('data-type') || '';
						const serviceStart 		= 	field.getAttribute('data-start') || '';
						const serviceEnd		= 	field.getAttribute('data-end') || '';
						const serviceAmount 	= 	field.getAttribute('data-amount') || '';
						const serviceDivision 	= 	field.getAttribute('data-division') || '';
						const serviceActive 	= 	field.getAttribute('data-active') || '';

						const serviceCard = document.createElement('div');
						serviceCard.className = 'border rounded p-2 mb-2 bg-gray-100';

						const serviceTop = document.createElement('div');
						serviceTop.className = 'flex justify-between items-center';

						const serviceTitle = document.createElement('div');
						serviceTitle.textContent = serviceName;

						const serviceButtons = document.createElement('div');
						serviceButtons.className = 'flex space-x-2';

						const editServiceButton = document.createElement('button');
						editServiceButton.type = 'button';
						editServiceButton.className = 'px-2 py-1 text-sm bg-yellow-200 rounded';
						editServiceButton.textContent = 'Rediger Tjeneste';

						const deleteServiceButton = document.createElement('button');
						deleteServiceButton.type = 'button';
						deleteServiceButton.className = 'px-2 py-1 text-sm bg-red-300 rounded';
						deleteServiceButton.textContent = 'Slet Tjeneste';

						serviceButtons.appendChild(editServiceButton);
						serviceButtons.appendChild(deleteServiceButton);
						serviceTop.appendChild(serviceTitle);
						serviceTop.appendChild(serviceButtons);
						serviceCard.appendChild(serviceTop);

						const serviceDetails = document.createElement('div');
						serviceDetails.className = 'text-sm text-gray-700 mt-2';
						serviceDetails.textContent = '';
						const typeLabels = {
							'Billeter': 'Billeter',
							'Kuponer': 'Kuponer',
							'Banner': 'Banner',
							'LogoTrojer': 'Logo på trøjer',
							'LogoBukser': 'Logo på bukser'
						};
						const displayType = typeLabels[serviceType] || "";
						const displayStart = formatDisplayDate(serviceStart);
						const displayEnd = formatDisplayDate(serviceEnd);
						const displayStatus = serviceActive === "true" ? "Ikke opfyldt" : "Opfyldt";
						const formatField = (label, value) => { 
							const div = document.createElement('div'); 
							div.textContent = label + ' ' + (value || ''); 
							return div; 
						};
						serviceDetails.appendChild(formatField('Type:', displayType));
						serviceDetails.appendChild(formatField('Start:', displayStart));
						serviceDetails.appendChild(formatField('End:', displayEnd));
						if (serviceType === 'LogoTrojer' || serviceType === 'LogoBukser') {
							serviceDetails.appendChild(formatField('Division:', serviceDivision));
						} else {
							serviceDetails.appendChild(formatField('Antal:', serviceAmount));
						}
						
						serviceDetails.appendChild(formatField('Status:', displayStatus));
						serviceCard.appendChild(serviceDetails);

						const serviceEditBlock = document.createElement('div');
						serviceEditBlock.style.display = 'none';
						serviceEditBlock.className = 'mt-2';
						const updateServiceForm = document.createElement('form');
						updateServiceForm.action = '/api/service/update';
						updateServiceForm.method = 'POST';
						const serviceIdInput = document.createElement('input'); 
						serviceIdInput.type = 'hidden'; 
						serviceIdInput.name = 'id'; 
						serviceIdInput.value = serviceId; 
						updateServiceForm.appendChild(serviceIdInput);

						const serviceContractId = document.createElement('input'); 
						serviceContractId.type = 'hidden'; 
						serviceContractId.name = 'contractId'; 
						serviceContractId.value = field.getAttribute('data-contract-id'); 
						updateServiceForm.appendChild(serviceContractId);

						const serviceNameLabel = document.createElement('label'); 
						serviceNameLabel.className = 'block text-sm'; 
						serviceNameLabel.textContent = 'Navn'; 
						updateServiceForm.appendChild(serviceNameLabel);

						const serviceNameInput = document.createElement('input'); 
						serviceNameInput.name = 'name'; 
						serviceNameInput.type = 'text'; 
						serviceNameInput.value = serviceName; 
						serviceNameInput.className='border rounded px-2 py-1 w-full'; 
						updateServiceForm.appendChild(serviceNameInput);

						const serviceTypeLabel = document.createElement('label'); 
						serviceTypeLabel.className='block text-sm'; 
						serviceTypeLabel.textContent='Type'; 
						updateServiceForm.appendChild(serviceTypeLabel);

						const serviceTypeSelect = document.createElement('select'); 
						serviceTypeSelect.name='type'; 
						serviceTypeSelect.className='border rounded px-2 py-1 w-full';

						const templateType = document.getElementById('addServiceType');
						if (templateType) {
							const options = templateType.querySelectorAll('option');
							options.forEach(option => { 
								serviceTypeSelect.appendChild(option.cloneNode(true)); 
							});
							if (serviceType) serviceTypeSelect.value = serviceType;
						}
						updateServiceForm.appendChild(serviceTypeSelect);

						const serviceAmountContainer = document.createElement('div'); 
						serviceAmountContainer.id = 's-amount-wrapper-' + serviceId; 
						updateServiceForm.appendChild(serviceAmountContainer);

						function updateServiceAmount() {

							serviceAmountContainer.textContent = '';
							if (serviceTypeSelect.value === 'LogoTrojer' || serviceTypeSelect.value === 'LogoBukser') {
								const divisionLabel = document.createElement('label'); 
								divisionLabel.className='block text-sm'; 
								divisionLabel.textContent='Divisionen'; 
								serviceAmountContainer.appendChild(divisionLabel);

								const divSelect = document.createElement('select'); 
								divSelect.name='division'; 
								divSelect.className='border rounded px-2 py-1 w-full';

								const prev = parseInt(serviceDivision) || 0;

								for (let i = 1; i <= 10; i++) { 
									const option = document.createElement('option'); 
									option.value = String(i); 
									option.textContent = String(i); 
									if(i === prev) option.selected = true;  
									divSelect.appendChild(option);
								}
								serviceAmountContainer.appendChild(divSelect);
							} else {
								const amountLabel 		= document.createElement('label'); 
								amountLabel.className 	='block text-sm'; 
								amountLabel.textContent ='Antal'; 

								const amountInput 		= document.createElement('input'); 
								amountInput.name 		='amount'; 
								amountInput.type 		='number'; 
								amountInput.value 		= (serviceAmount || '0'); 
								amountInput.className 	= 'border rounded px-2 py-1 w-full'; 

								serviceAmountContainer.appendChild(amountLabel);
								serviceAmountContainer.appendChild(amountInput);
							}
						}
						updateServiceAmount();
						serviceTypeSelect.addEventListener('change', updateServiceAmount);

						const serviceStartLabel = document.createElement('label'); 
						serviceStartLabel.className='block text-sm'; 
						serviceStartLabel.textContent='Start Dato'; 
						updateServiceForm.appendChild(serviceStartLabel);

						const serviceStartInput = document.createElement('input'); 
						serviceStartInput.name='startDate'; 
						serviceStartInput.type='date'; 
						serviceStartInput.value=serviceStart; 
						serviceStartInput.className='border rounded px-2 py-1 w-full'; 
						updateServiceForm.appendChild(serviceStartInput);

						const serviceEndLabel = document.createElement('label'); 
						serviceEndLabel.className='block text-sm'; 
						serviceEndLabel.textContent='Slut Dato'; 
						updateServiceForm.appendChild(serviceEndLabel);

						const serviceEndInput = document.createElement('input'); 
						serviceEndInput.name='endDate'; 
						serviceEndInput.type='date'; 
						serviceEndInput.value=serviceEnd; 
						serviceEndInput.className='border rounded px-2 py-1 w-full'; 
						updateServiceForm.appendChild(serviceEndInput);

						const serviceStatusLabel = document.createElement('label'); 
						serviceStatusLabel.className='block text-sm'; 
						serviceStatusLabel.textContent='Status'; 
						updateServiceForm.appendChild(serviceStatusLabel);

						const serviceStatusSelect = document.createElement('select'); 
						serviceStatusSelect.name='active'; 
						serviceStatusSelect.className='border rounded px-2 py-1 w-full';

						const serviceOptionTrue = document.createElement('option'); 
						serviceOptionTrue.value = 'true'; 
						serviceOptionTrue.textContent = 'Ikke opfyldt';

						const serviceOptionFalse = document.createElement('option'); 
						serviceOptionFalse.value = 'false'; 
						serviceOptionFalse.textContent = 'Opfyldt';

						serviceStatusSelect.appendChild(serviceOptionTrue); 
						serviceStatusSelect.appendChild(serviceOptionFalse); 
						serviceStatusSelect.value = serviceActive === "true" ? "true" : "false";
						updateServiceForm.appendChild(serviceStatusSelect);

						const serviceButtonRow = document.createElement('div'); 
						serviceButtonRow.className='flex justify-end space-x-2 mt-2';

						const serviceButtonCancel = document.createElement('button'); 
						serviceButtonCancel.type='button'; 
						serviceButtonCancel.className='px-3 py-1 bg-gray-200 rounded'; 
						serviceButtonCancel.textContent='Cancel';

						const serviceSaveButton = document.createElement('button'); 
						serviceSaveButton.type='submit'; 
						serviceSaveButton.className='px-3 py-1 bg-green-500 text-white rounded'; 
						serviceSaveButton.textContent='Save';
						serviceButtonRow.appendChild(serviceButtonCancel); 
						serviceButtonRow.appendChild(serviceSaveButton); 
						updateServiceForm.appendChild(serviceButtonRow);
						serviceEditBlock.appendChild(updateServiceForm);
						serviceCard.appendChild(serviceEditBlock);

						editServiceButton.addEventListener('click', () => { serviceEditBlock.style.display = serviceEditBlock.style.display === 'none' ? 'block' : 'none'; });
						serviceButtonCancel.addEventListener('click', () => { serviceEditBlock.style.display = 'none'; });
						deleteServiceButton.addEventListener('click', () => { if(confirm('Slet denne tjeneste?')) postDeleteService(serviceId, id); });

						servicesList.appendChild(serviceCard);
					}
				});
			}
		} catch (error) {
			console.error(error);
		}

		const editBlock 				= 	document.createElement('div');
		editBlock.id 					= 	`editform-${index}`;
		editBlock.style.display 		= 	'none';
		editBlock.className 			= 	'mt-2 text-sm text-gray-700 border-t pt-2';

		const updateContractForm 		= 	document.createElement('form');
		updateContractForm.action 		= 	'/api/contract/update';
		updateContractForm.method 		= 	'POST';
		updateContractForm.className 	= 	'space-y-2';
		updateContractForm.enctype 		= 	'multipart/form-data';

		const hiddenId 					= 	document.createElement('input'); 
		hiddenId.type 					= 	'hidden'; 
		hiddenId.name 					= 	'id'; 
		hiddenId.value 					= 	id;

		updateContractForm.appendChild(hiddenId); 

		function addLabelAndElement(parent, labelText, element) {
			const label = document.createElement('label'); 
			label.className = 'block text-sm'; 
			label.textContent = labelText; 
			parent.appendChild(label); 
			parent.appendChild(element);
		}

		const sponsorSelect 	= 	document.createElement('select'); 
		sponsorSelect.id 		= 	`sponsor-select-${index}`; 
		sponsorSelect.name 		= 	'sponsorId'; 
		sponsorSelect.className = 	'border rounded px-2 py-1 w-full'; 
		addLabelAndElement(updateContractForm, 'Sponsor', sponsorSelect);

		const nameInput 	= 	document.createElement('input'); 
		nameInput.name		= 	'name'; 
		nameInput.type 		= 	'text'; 
		nameInput.value 	= 	name; 
		nameInput.className = 	'border rounded px-2 py-1 w-full'; 
		addLabelAndElement(updateContractForm, 'Navn', nameInput);
		nameInput.required = true;

		const typeInput 	= 	document.createElement('input'); 
		typeInput.name 		= 	'type'; 
		typeInput.type 		= 	'text'; 
		typeInput.value 	= 	type; 
		typeInput.className = 	'border rounded px-2 py-1 w-full'; 
		addLabelAndElement(updateContractForm, 'Type', typeInput);

		const startInput 		= 	document.createElement('input'); 
		startInput.name 		= 	'startDate'; 
		startInput.type 		= 	'date'; 
		startInput.value		= 	start || '' ; 
		startInput.className 	= 	'border rounded px-2 py-1 w-full'; 
		addLabelAndElement(updateContractForm, 'Start Dato', startInput);

		const endInput 		= 	document.createElement('input'); 
		endInput.name		= 	'endDate'; 
		endInput.type 		= 	'date'; 
		endInput.value 		= 	end || '' ; 
		endInput.className 	= 	'border rounded px-2 py-1 w-full'; 
		addLabelAndElement(updateContractForm, 'Slut Dato', endInput);

		const paymentInput 		= 	document.createElement('input'); 
		paymentInput.name 		= 	'payment'; 
		paymentInput.type 		= 	'number'; 
		paymentInput.value 		= 	payment;
		paymentInput.className 	= 	'border rounded px-2 py-1 w-full'; 
		addLabelAndElement(updateContractForm, 'Beløb', paymentInput);
		
		
		const statusSelect 			= 	document.createElement('select'); 
		statusSelect.id 			= 	`status-select-${index}`; 
		statusSelect.name 			= 	'status'; 
		statusSelect.className 		= 	'border rounded px-2 py-1 w-full';
		const optionTrue 			= 	document.createElement('option'); 
		optionTrue.value 			= 	'true'; 
		optionTrue.textContent 		= 	'True';
		const optionFalse 			= 	document.createElement('option'); 
		optionFalse.value 			= 	'false'; 
		optionFalse.textContent 	= 	'False';
		
		const pdfInput 		= 	document.createElement('input'); 
		pdfInput.type 		= 	'file'; 
		pdfInput.name 		= 	'pdffile'; 
		pdfInput.accept 	= 	'application/pdf'; 
		pdfInput.className 	= 	'border rounded px-2 py-1 w-full'; 
		addLabelAndElement(updateContractForm, 'Upload PDF', pdfInput);

		const buttonRow 			= 	document.createElement('div'); 
		buttonRow.className 		= 	'flex justify-end space-x-2 mt-2';
		const cancelButton 			= 	document.createElement('button'); 
		cancelButton.type 			= 	'button'; 
		cancelButton.className 		= 	'px-3 py-1 bg-gray-200 rounded'; 
		cancelButton.textContent 	= 	'Cancel';
		const saveButton 			= 	document.createElement('button'); 
		saveButton.type 			= 	'submit'; 
		saveButton.className 		= 	'px-3 py-1 bg-green-500 text-white rounded'; 
		saveButton.textContent 		= 	'Save';
		buttonRow.appendChild(cancelButton); 
		buttonRow.appendChild(saveButton); 
		updateContractForm.appendChild(buttonRow);
		editBlock.appendChild(updateContractForm);

		container.appendChild(topRow);
		container.appendChild(contractDetails);
		container.appendChild(servicesContainer);
		container.appendChild(editBlock);

		addServiceButton.addEventListener('click', () => {
			try {
				const modal = document.getElementById('addServiceModal');
				if (!modal) return;
				document.getElementById('addServiceContractId').value = id;
				document.getElementById('addServiceContractName').textContent = name || ('#' + id);
				const form = modal.querySelector('form'); 
				if (form) form.reset();

				function ensureAddWrapper() {
				const select = document.getElementById('addServiceType');
				const wrapper = document.getElementById('amountOrDivisionWrapper');
				if (!select || !wrapper) return;
				function render() {
					wrapper.textContent = '';
					if (select.value === 'LogoTrojer' || select.value === 'LogoBukser') {
						const label = document.createElement('label'); 
						label.className ='block text-sm'; 
						label.textContent ='Divisionen'; 
						wrapper.appendChild(label);
						const select = document.createElement('select'); 
						select.name ='division'; 
						select.className ='border rounded px-2 py-1 w-full';
						for (let i = 1; i <= 10; i++) { 
							const option = document.createElement('option'); 
							option.value = String(i); 
							option.textContent = String(i); 
							select.appendChild(option); 
						}
						wrapper.appendChild(select);
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
				select.removeEventListener && select.removeEventListener('change', render);
				select.addEventListener('change', render);
				render();
			}
				ensureAddWrapper();
				modal.style.display = 'flex';
			} catch (error) { 
				console.error(error); 
			}
		});

		editButton.addEventListener('click', () => { editBlock.style.display = editBlock.style.display === 'none' ? 'block' : 'none'; });
		deleteButton.addEventListener('click', () => { 
			if (confirm('Slet denne kontrakt? Dette kan ikke fortrydes.')) { 
				postDeleteContract(id, sponsorId); 
			} 
		});
		cancelButton.addEventListener('click', () => { editBlock.style.display = 'none'; });

		contractList.appendChild(container);

		try {
			const template = document.getElementById('sponsorOptionsTemplate');
			if (template) {
				const select = document.getElementById('sponsor-select-' + index);
				if (select) {
					const options = template.querySelectorAll('option');
					options.forEach(option => { 
						select.appendChild(option.cloneNode(true)); 
					});
					select.value = field.getAttribute('data-sponsor-id');
				}
			}
			const statusSelect = document.getElementById('status-select-' + index);
			if (statusSelect) statusSelect.value = String(status) === 'True' ? 'Aktiv' : 'Inaktiv';
		} catch (error) {
			console.error(error);
		}
	});
};

function postDeleteContract(contractId, sponsorId) {
	try {
		const form 			= 	document.createElement('form');
		form.method 		= 	'POST';
		form.action 		= 	'/api/contract/delete';
		form.style.display 	= 	'none';

		const inputId 		= 	document.createElement('input'); 
		inputId.type 		= 	'hidden'; 
		inputId.name 		= 	'contractId'; 
		inputId.value 		= 	contractId; ;
		
		const inputSponsor 	= 	document.createElement('input'); 
		inputSponsor.type 	= 	'hidden'; 
		inputSponsor.name 	= 	'sponsorId'; 
		inputSponsor.value 	= 	sponsorId || ''; 

		form.appendChild(inputId)
		form.appendChild(inputSponsor);
		document.body.appendChild(form);
		form.submit();
	} catch (error) {
		console.error(error);
	}
}


function toggleContractEdit(i) {
	const id = 'contractEditForm-' + i;
	const element = document.getElementById(id);
	if (!element) return;
	element.style.display = element.style.display === 'none' || element.style.display === '' ? 'block' : 'none';
}

