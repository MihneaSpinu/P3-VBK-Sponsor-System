// Sponsors modal behavior (extracted from homepage inline script)
document.addEventListener('DOMContentLoaded', function () {
  try {
    const modal = document.getElementById('Smodal');
    if (!modal) return;
    const modalContent = modal.querySelector('.SponsorModal');
    const closeBtns = modal.querySelectorAll('.CloseModal');

    document.querySelectorAll('.sponsor-open-btn').forEach(function (btn) {
      btn.addEventListener('click', function () {
        const sponsorId = btn.getAttribute('data-sponsor-id');
        const template = document.getElementById('sponsor-modal-' + sponsorId);
        if (!template) return;

        // Clear existing content safely and clone template children
        if (modalContent) modalContent.textContent = '';
        Array.from(template.children).forEach(function (c) { if (modalContent) modalContent.appendChild(c.cloneNode(true)); });

        // Try to ensure modal is visible (some styles use display:flex)
        try { modal.style.display = 'flex'; } catch (err) { /* ignore */ }
      });
    });

    closeBtns.forEach(function (cb) {
      cb.addEventListener('click', function (e) {
        e.preventDefault();
        if (modalContent) modalContent.textContent = '';
        try { modal.style.display = 'none'; } catch (err) { /* ignore */ }
      });
    });
  } catch (e) {
    console.error('sponsors.js initialization error', e);
  }
});
// Common sponsor-page helpers: filters, modals, input validation
function toggleSponsorEdit(i) {
	const id = 'sponsorEditForm-' + i;
	const el = document.getElementById(id);
	if (!el) return;
	el.style.display = el.style.display === 'none' || el.style.display === '' ? 'block' : 'none';
}

function closeModal(id) {
	const el = document.getElementById(id);
	if (!el) return;
	el.style.display = 'none';
}

function openAddModal(buttonEl) {
	try {
		const sid = buttonEl.getAttribute('data-sponsor-id');
		const sname = buttonEl.getAttribute('data-sponsor-name') || '';
		const idEl = document.getElementById('addModalSponsorId');
		const nameEl = document.getElementById('addModalSponsorName');
		if (idEl) idEl.value = sid;
		if (nameEl) nameEl.textContent = sname;
		const modal = document.getElementById('addContractModal');
		if (modal) modal.style.display = 'flex';
	} catch (e) {
		console.error('openAddModal error', e);
	}
}

function openViewModal(buttonEl) {
	try {
		const sid = buttonEl.getAttribute('data-sponsor-id');
		const sname = buttonEl.getAttribute('data-sponsor-name') || '';
		const nameEl = document.getElementById('viewModalSponsorName');
		if (nameEl) nameEl.textContent = sname || ('#' + sid);
		if (typeof populateViewContracts === 'function') populateViewContracts(sid);
		const modal = document.getElementById('viewContractsModal');
		if (modal) modal.style.display = 'flex';
	} catch (e) {
		console.error('openViewModal error', e);
	}
}

// Basic html escaping for other modules
function escapeHtml(s) {
	if (!s && s !== 0) return '';
	return String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}

// Attach phone input handlers and sponsor filter behavior on load
(function () {
	function attachPhoneHandlers(el) {
		if (!el) return;
		el.addEventListener('keydown', function (e) {
			const k = e.key;
			if (k.length !== 1) return;
			if (!/^[0-9]$/.test(k)) {
				e.preventDefault();
			}
		});
		el.addEventListener('paste', function (e) {
			e.preventDefault();
			const text = (e.clipboardData || window.clipboardData).getData('text') || '';
			const filtered = text.replace(/[^0-9]/g, '');
			const start = el.selectionStart;
			const end = el.selectionEnd;
			const before = el.value.slice(0, start);
			const after = el.value.slice(end);
			el.value = before + filtered + after;
			const caret = start + filtered.length;
			el.setSelectionRange(caret, caret);
		});
		el.addEventListener('input', function () {
			const cleaned = el.value.replace(/[^0-9]/g, '');
			if (cleaned !== el.value) {
				const pos = Math.max(0, el.selectionStart - (el.value.length - cleaned.length));
				el.value = cleaned;
				el.setSelectionRange(pos, pos);
			}
		});
	}

	// filter function
	function applySponsorFilter() {
		const sel = document.getElementById('sponsorFilter');
		if (!sel) return;
		const v = sel.value;
		document.querySelectorAll('.sponsor-item').forEach(function (el) {
			const status = el.getAttribute('data-active');
			if (v === 'all') {
				el.style.display = '';
			} else if (v === 'active') {
				el.style.display = status === 'true' || status === 'True' ? '' : 'none';
			} else if (v === 'inactive') {
				el.style.display = status === 'false' || status === 'False' ? '' : 'none';
			}
		});
	}

	// init on DOMContentLoaded
	document.addEventListener('DOMContentLoaded', function () {
		attachPhoneHandlers(document.getElementById('phoneNumber'));
		document.querySelectorAll('input[name="phoneNumber"]').forEach(attachPhoneHandlers);
		const filterEl = document.getElementById('sponsorFilter');
		if (filterEl) {
			filterEl.value = 'active';
			applySponsorFilter();
			filterEl.addEventListener('change', applySponsorFilter);
		}
	});
})();
