import React, { useState, useEffect } from 'react';
import './SimpleNavigator.css';
import MultiMapViewer from './MultiMapViewer';

import { CONTROL_SERVER_URL } from '../constants';

const Button = ({ onClick, children, variant = 'default' }) => (
  <button
    onClick={onClick}
    className={`px-4 py-2 rounded text-white ${variant === 'outline' ? 'bg-gray-500 hover:bg-gray-600' : 'bg-blue-500 hover:bg-blue-600'}`}
  >
    {children}
  </button>
);

const UGVControlComponent = () => {
  const [ugvs, setUgvs] = useState([]);
  const [selectedUGV, setSelectedUGV] = useState('');
  const [x, setX] = useState('');
  const [y, setY] = useState('');
  const [theta, setTheta] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Fetch UGVs from the API
  useEffect(() => {
    fetch(CONTROL_SERVER_URL+'/ugv')
      .then(response => response.json())
      .then(data => {
        // Filter UGVs with status "ONLINE"
        const onlineUgvs = data.filter(ugv => ugv.status === 'ONLINE');
        setUgvs(onlineUgvs);
      })
      .catch(error => console.error('Error fetching UGVs:', error));
  }, []);

  // Handle form submission
  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!selectedUGV || !x || !y || !theta) {
      alert('Please fill all fields and select a UGV.');
      return;
    }

    setIsSubmitting(true);

    const payload = {
      ugvId: selectedUGV,
      targetPose: { x: parseFloat(x), y: parseFloat(y), theta: parseFloat(theta) },
      callbackURL: 'http://ui-service:8080/action/completed'
    };

    try {
      const response = await fetch(CONTROL_SERVER_URL+'/navigation-request', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (!response.ok) {
        throw new Error('Failed to submit navigation request');
      }

      alert('Navigation request submitted successfully!');
    } catch (error) {
      console.error('Error submitting navigation request:', error);
      alert('Failed to submit navigation request.');
    } finally {
      // The callback URL will unfreeze the button when hit
    }
  };

  // Simulate callback URL hit (for demonstration purposes)
  useEffect(() => {
    if (isSubmitting) {
      const timeout = setTimeout(() => {
        setIsSubmitting(false);
      }, 5000); // Simulate a 5-second delay for the callback

      return () => clearTimeout(timeout);
    }
  }, [isSubmitting]);

  return (
    <div class="container">
      <h2>UGV Navigation Control</h2>
      <div className='container'>
        <form onSubmit={handleSubmit}>
          <table>
            <tbody>
              <tr>
                <td>
                  <label>Select UGV:</label>
                </td>
                <td>
                  <select
                    value={selectedUGV}
                    onChange={(e) => setSelectedUGV(e.target.value)}
                    required
                  >
                    <option value="" disabled>Select a UGV</option>
                    {ugvs.map(ugv => (
                      <option key={ugv.id} value={ugv.id}>
                        {ugv.name} (ID: {ugv.id})
                      </option>
                    ))}
                  </select>
                </td>
              </tr>
              <tr>
                <td>
                  <label>X Coordinate:</label>
                </td>
                <td>
                  <input
                    type="number"
                    value={x}
                    onChange={(e) => setX(e.target.value)}
                    required
                  />
                </td>
              </tr>
              <tr>
                <td>
                  <label>Y Coordinate:</label>
                </td>
                <td>
                  <input
                    type="number"
                    value={y}
                    onChange={(e) => setY(e.target.value)}
                    required
                  />
                </td>
              </tr>
              <tr>
                <td>
                  <label>Theta (Orientation):</label>
                </td>
                <td>
                  <input
                    type="number"
                    value={theta}
                    onChange={(e) => setTheta(e.target.value)}
                    required
                  />
                </td>
              </tr>
              <tr>
                <td colSpan="2" style={{ textAlign: 'center' }}>
                  <Button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? 'Submitting...' : 'Submit'}
                  </Button>
                </td>
              </tr>
            </tbody>
          </table>
        </form>
      </div>
    </div>
  );
};

export default UGVControlComponent;