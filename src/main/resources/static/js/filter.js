document.addEventListener("DOMContentLoaded", () => {
  const openFilterBtn = document.getElementById("openFilterBtn");
  const filterModal = document.getElementById("filterModal");
  const closeFilterBtn = document.getElementById("closeFilterBtn");
  const closeFilterBtnBottom = document.getElementById("closeFilterBtnBottom");
  // Modal open/close
  function openModal() {
    if (!filterModal) return;
    filterModal.classList.remove("hidden");
    filterModal.classList.add("flex");
  }
  function closeModal() {
    if (!filterModal) return;
    filterModal.classList.add("hidden");
    filterModal.classList.remove("flex");
  }

  if (openFilterBtn) openFilterBtn.addEventListener("click", openModal);
  if (closeFilterBtn) closeFilterBtn.addEventListener("click", closeModal);
  if (closeFilterBtnBottom)
    closeFilterBtnBottom.addEventListener("click", closeModal);
  if (filterModal)
    filterModal.addEventListener("click", (e) => {
      if (e.target === filterModal) closeModal();
    });

  // Filtering logic
  const applyFilterBtn = document.getElementById("applyFilterBtn");
  const clearFilterBtn = document.getElementById("clearFilterBtn");

  function parsePaymentText(text) {
    // e.g. "25.000 kr." -> 25000
    if (!text) return null;
    const cleaned = text
      .replace(/[\.\skr]/g, "")
      .replace(",", ".")
      .trim();
    const num = parseFloat(cleaned);
    return isNaN(num) ? null : num;
  }

  function parseDateRange(text) {
    // e.g. "15/09/2025 - 02/02/2026" -> {from: Date, to: Date}
    if (!text) return null;
    const parts = text.split("-").map((s) => s.trim());
    if (parts.length < 2) return null;
    const [fromStr, toStr] = parts;
    const parse = (dstr) => {
      const [dd, mm, yyyy] = dstr.split("/").map((s) => s.trim());
      return new Date(Number(yyyy), Number(mm) - 1, Number(dd));
    };
    try {
      return { from: parse(fromStr), to: parse(toStr) };
    } catch (e) {
      return null;
    }
  }

  function getSponsorCards() {
    return Array.from(document.querySelectorAll(".space-y-4 > div"));
  }

  function cardMatchesFilters(card, filters) {
    const nameEl = card.querySelector(".grid > div:first-child div");
    const descEl = card.querySelector(".grid > div:nth-child(2) div");
    const paymentEls = Array.from(
      card.querySelectorAll(
        ".grid > div:nth-child(3) div > div, .grid > div:nth-child(3) .bg-gray-200"
      )
    );
    // fallback selectors because structure varies
    const paymentTexts = paymentEls.length
      ? paymentEls.map((e) => e.textContent || e.innerText)
      : Array.from(
          card.querySelectorAll(".grid > div:nth-child(3) .bg-gray-200")
        ).map((e) => e.textContent || e.innerText);
    const dateEls = Array.from(
      card.querySelectorAll(
        ".grid > div:nth-child(4) div, .grid > div:nth-child(4) .bg-gray-200"
      )
    );
    const dateTexts = dateEls.map((e) => e.textContent || e.innerText);

    const name = (
      (nameEl && (nameEl.textContent || nameEl.innerText)) ||
      ""
    ).trim();
    const desc = (
      (descEl && (descEl.textContent || descEl.innerText)) ||
      ""
    ).trim();

    // Name filter
    if (filters.name) {
      if (!name.toLowerCase().includes(filters.name.toLowerCase()))
        return false;
    }

    if (filters.description) {
      if (!desc.toLowerCase().includes(filters.description.toLowerCase()))
        return false;
    }

    // Payment filters: check any payment in the card satisfies the range
    if (filters.minPayment != null || filters.maxPayment != null) {
      const payments = paymentTexts
        .map((t) => parsePaymentText(t))
        .filter((p) => p != null);
      if (payments.length === 0) return false;
      const meets = payments.some((p) => {
        if (filters.minPayment != null && p < filters.minPayment) return false;
        if (filters.maxPayment != null && p > filters.maxPayment) return false;
        return true;
      });
      if (!meets) return false;
    }

    // Date range filter: require at least one active period that overlaps the requested range
    if (filters.dateFrom || filters.dateTo) {
      const ranges = dateTexts
        .map((t) => parseDateRange(t))
        .filter((r) => r != null);
      if (ranges.length === 0) return false;
      const userFrom = filters.dateFrom ? new Date(filters.dateFrom) : null;
      const userTo = filters.dateTo ? new Date(filters.dateTo) : null;
      const overlaps = ranges.some((r) => {
        const start = r.from,
          end = r.to;
        if (userFrom && end < userFrom) return false;
        if (userTo && start > userTo) return false;
        return true;
      });
      if (!overlaps) return false;
    }

    return true;
  }

  function applyFilters() {
    const name = document.getElementById("filterName").value.trim();
    const description = document
      .getElementById("filterDescription")
      .value.trim();
    const minPaymentRaw = document.getElementById("filterMinPayment").value;
    const maxPaymentRaw = document.getElementById("filterMaxPayment").value;
    const dateFrom = document.getElementById("filterDateFrom").value;
    const dateTo = document.getElementById("filterDateTo").value;

    const filters = {
      name: name || null,
      description: description || null,
      minPayment: minPaymentRaw ? Number(minPaymentRaw) : null,
      maxPayment: maxPaymentRaw ? Number(maxPaymentRaw) : null,
      dateFrom: dateFrom || null,
      dateTo: dateTo || null,
    };

    const cards = getSponsorCards();
    cards.forEach((card) => {
      const match = cardMatchesFilters(card, filters);
      if (match) {
        card.style.display = "";
      } else {
        card.style.display = "none";
      }
    });

    closeModal();
    // After filtering, keep current sort
    applyCurrentSort();
  }

  function clearFilters() {
    document.getElementById("filterForm").reset();
    const cards = getSponsorCards();
    cards.forEach((card) => (card.style.display = ""));
  }

  if (applyFilterBtn) applyFilterBtn.addEventListener("click", applyFilters);
  if (clearFilterBtn) clearFilterBtn.addEventListener("click", clearFilters);

  // Accessibility: close modal with Escape
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && !filterModal.classList.contains("hidden"))
      closeModal();
  });

  // Sorting logic
  const sortSelect = document.getElementById("sortSelect");
  let currentSort = "";

  function getCardName(card) {
    const el = card.querySelector(".grid > div:first-child div");
    return ((el && (el.textContent || el.innerText)) || "")
      .trim()
      .toLowerCase();
  }

  function getCardMaxPayment(card) {
    const paymentEls = Array.from(
      card.querySelectorAll(
        ".grid > div:nth-child(3) div > div, .grid > div:nth-child(3) .bg-gray-200"
      )
    );
    const texts = paymentEls.map((e) => e.textContent || e.innerText);
    const nums = texts.map((t) => parsePaymentText(t)).filter((n) => n != null);
    if (nums.length === 0) return -Infinity;
    return Math.max(...nums);
  }

  function getCardEarliestStart(card) {
    const dateEls = Array.from(
      card.querySelectorAll(
        ".grid > div:nth-child(4) div, .grid > div:nth-child(4) .bg-gray-200"
      )
    );
    const texts = dateEls.map((e) => e.textContent || e.innerText);
    const ranges = texts.map((t) => parseDateRange(t)).filter((r) => r != null);
    if (ranges.length === 0) return new Date(8640000000000000); // far future
    return ranges.reduce(
      (acc, r) => (acc < r.from ? acc : r.from),
      ranges[0].from
    );
  }

  function applyCurrentSort() {
    if (!currentSort) return;
    const container = document.querySelector(".space-y-4");
    const cards = Array.from(container.querySelectorAll(":scope > div"));
    // Only sort visible cards; keep hidden ones at end in original order
    const visible = cards.filter((c) => c.style.display !== "none");
    const hidden = cards.filter((c) => c.style.display === "none");

    let comparator;
    switch (currentSort) {
      case "name-asc":
        comparator = (a, b) => getCardName(a).localeCompare(getCardName(b));
        break;
      case "name-desc":
        comparator = (a, b) => getCardName(b).localeCompare(getCardName(a));
        break;
      case "payment-desc":
        comparator = (a, b) => getCardMaxPayment(b) - getCardMaxPayment(a);
        break;
      case "date-asc":
        comparator = (a, b) =>
          getCardEarliestStart(a) - getCardEarliestStart(b);
        break;
      default:
        comparator = null;
    }

    if (comparator) {
      visible.sort((a, b) => {
        const res = comparator(a, b);
        return res;
      });
    }

    // Re-append in order: visible first (sorted) then hidden in original order
    visible.concat(hidden).forEach((c) => container.appendChild(c));
  }

  if (sortSelect)
    sortSelect.addEventListener("change", (e) => {
      currentSort = e.target.value;
      applyCurrentSort();
    });
});
