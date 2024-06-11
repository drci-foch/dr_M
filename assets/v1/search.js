window.Vaadin.search={
    initLazy: function (c) {
        if (c.$connector) {
            return;
        }
        c.$connector = {};
		var highlights, currentIndex = -1;
		c.$connector.manageHighlight = function() {
			currentIndex = -1;

			// Find all the highlighted elements
			highlights = document.querySelectorAll('.highlight2');

		}

		c.$connector.goNextHighlight = function() {
			goToNextHighlight();
		}


        // Function to move to the next highlight
        function goToNextHighlight() {
            if (highlights.length === 0) {
                return; // No highlights found
            }
			currentIndex = (currentIndex + 1) % highlights.length;

            const highlightElement = highlights[currentIndex];
            highlightElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }

        // Step 3: Event Listener for Double-Click
        // document.getElementById('content').addEventListener('dblclick', goToNextHighlight);

    }

}