#### Enhancements:
- Save to file / Open from file
	- Upon startup, the File -> Restore button is greyed out
	- Once File -> Save is clicked (or ctrl-s is pressed) a canvas state file is saved to `src/output.txt`
	- If `src/output.txt` is present and File -> Restore is clicked, the canvas is restored to the state in `src/output.txt`
- Added shear graphics transformation
