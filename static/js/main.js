document.getElementById('city').addEventListener('change', function() {
    const city = this.value;
    const neighborhoodSelect = document.getElementById('neighborhood');
    
    // AJAX ile mahalleleri yükle
    fetch(`/get_neighborhoods/${city}`)
        .then(response => response.json())
        .then(neighborhoods => {
            neighborhoodSelect.innerHTML = '';
            neighborhoods.forEach(neighborhood => {
                const option = document.createElement('option');
                option.value = neighborhood;
                option.textContent = neighborhood;
                neighborhoodSelect.appendChild(option);
            });
        });
}); 