
const openFilterButton = document.getElementById("openFilterBtn");
const filterModal = document.getElementById("filterModal");
const closeFilterButton = document.getElementById("closeFilterBtn");
const closeFilterButtonBottom = document.getElementById("closeFilterBtnBottom");

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

if (openFilterButton) openFilterButton.addEventListener("click", openModal);
if (closeFilterButton) closeFilterButton.addEventListener("click", closeModal);
if (closeFilterButtonBottom) closeFilterButtonBottom.addEventListener("click", closeModal);
if (filterModal)
filterModal.addEventListener("click", event => {
  if (event.target === filterModal) closeModal();
});


const applyFilterButton = document.getElementById("applyFilterBtn");
const clearFilterButton = document.getElementById("clearFilterBtn");

function parsePaymentText(text) {

  if (!text) return null;
  const cleaned = text
    .replace(/[\.\skr]/g, "")
    .replace(",", ".")
    .trim();
  const num = parseFloat(cleaned);
  return isNaN(num) ? null : num;
}

function parseDateRange(text) {
  if (!text) return null;
  const parts = text.split("-").map(string => string.trim());
  if (parts.length < 2) return null;
  const [fromString, toString] = parts;
  const parse = dateString => {
    const [dd, mm, yyyy] = dateString.split("/").map(string => string.trim());
    return new Date(Number(yyyy), Number(mm) - 1, Number(dd));
  };
  try {
    return {from: parse(fromString), to: parse(toString)};
  } catch (error) {
    return null;
  }
}

function getSponsorCards() {
  return Array.from(document.querySelectorAll(".space-y-4 > div"));
}

function cardMatchesFilters(card, filters) {
  const nameEl = card.querySelector(".grid > div:first-child div");
  const descEl = card.querySelector(".grid > div:nth-child(2) div");
  const paymentElements = Array.from(
    card.querySelectorAll(".grid > div:nth-child(3) div > div, .grid > div:nth-child(3) .bg-gray-200")
  );

  const paymentTexts = paymentElements.length
    ? paymentElements.map(element => element.textContent || element.innerText)
    : Array.from(
        card.querySelectorAll(".grid > div:nth-child(3) .bg-gray-200")
      ).map(element => element.textContent || element.innerText);
  const dateElements = Array.from(
    card.querySelectorAll(".grid > div:nth-child(4) div, .grid > div:nth-child(4) .bg-gray-200")
  );
  const dateTexts = dateElements.map(element => element.textContent || element.innerText);

  const name = ((nameEl && (nameEl.textContent || nameEl.innerText)) || "").trim();
  const desc = ((descEl && (descEl.textContent || descEl.innerText)) || "").trim();

  // Name filter
  if (filters.name) {
    if (!name.toLowerCase().includes(filters.name.toLowerCase()))
      return false;
  }

  if (filters.description) {
    if (!desc.toLowerCase().includes(filters.description.toLowerCase()))
      return false;
  }


  if (filters.dateFrom || filters.dateTo) {
    const ranges = dateTexts
      .map(text => parseDateRange(text))
      .filter(range => range != null);
      
    if (ranges.length === 0) return false;
    const userFrom = filters.dateFrom ? new Date(filters.dateFrom) : null;
    const userTo = filters.dateTo ? new Date(filters.dateTo) : null;
    const overlaps = ranges.some(range => {
      const start = range.from;
      const end = range.to;
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
  const description = document.getElementById("filterDescription").value.trim();
  const dateFrom = document.getElementById("filterDateFrom").value;
  const dateTo = document.getElementById("filterDateTo").value;

  const filters = {
    name: name || null,
    description: description || null,
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
  applyCurrentSort();
}

function clearFilters() {
  document.getElementById("filterForm").reset();
  const cards = getSponsorCards();
  cards.forEach(card => (card.style.display = ""));
}

if (applyFilterButton) applyFilterButton.addEventListener("click", applyFilters);
if (clearFilterButton) clearFilterButton.addEventListener("click", clearFilters);

document.addEventListener("keydown", event => {
  if (event.key === "Escape" && !filterModal.classList.contains("hidden"))
    closeModal();
});

// Sorting logic
const sortSelect = document.getElementById("sortSelect");
let currentSort = "";

function getCardName(card) {
  const element = card.querySelector(".grid > div:first-child div");
  return ((element.textContent || element.innerText) || "")
    .trim()
    .toLowerCase();
}


function getCardEarliestStart(card) {
  const dateElements = Array.from(
    card.querySelectorAll(".grid > div:nth-child(4) div, .grid > div:nth-child(4) .bg-gray-200")
  );
  const texts = dateElements.map(element => element.textContent || element.innerText);
  const ranges = texts.map(text => parseDateRange(text)).filter(range => range != null);
  if (ranges.length === 0) return new Date(8640000000000000);
  return ranges.reduce(
    (acc, range) => (acc < range.from ? acc : range.from),
    ranges[0].from
  );
}

function applyCurrentSort() {
  if (!currentSort) return;
  const container = document.querySelector(".space-y-4");
  const cards = Array.from(container.querySelectorAll(":scope > div"));

  const visible = cards.filter((card) => card.style.display !== "none");
  const hidden = cards.filter((card) => card.style.display === "none");

  let comparator;
  switch (currentSort) {
    case "name-asc":
      comparator = (a, b) => getCardName(a).localeCompare(getCardName(b));
      break;

    case "name-desc":
      comparator = (a, b) => getCardName(b).localeCompare(getCardName(a));
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
      return comparator(a, b);
    });
  }


  visible.concat(hidden).forEach(c => container.appendChild(c));
}

if (sortSelect) {
  sortSelect.addEventListener("change", event => {
    currentSort = event.target.value;
    applyCurrentSort();
  });
}

