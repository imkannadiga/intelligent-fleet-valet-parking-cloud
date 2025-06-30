import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { Toaster, toast } from 'react-hot-toast';

import { API_URL } from '../constants';

const Button = ({ onClick, children, variant = 'default' }) => (
  <button
    onClick={onClick}
    className={`px-4 py-2 rounded text-white ${variant === 'outline' ? 'bg-gray-500 hover:bg-gray-600' : 'bg-blue-500 hover:bg-blue-600'}`}
  >
    {children}
  </button>
);

const Card = ({ children }) => (
  <div className="p-4 border rounded shadow-md bg-white">{children}</div>
);

const CardContent = ({ children }) => <div className="space-y-2">{children}</div>;

export default function UGVList() {
  const [ugvs, setUgvs] = useState([]);

  useEffect(() => {
    const fetchUGVs = async () => {
      try {
        const response = await axios.get(`${API_URL}/ugv`);
        setUgvs(response.data);
      } catch (error) {
        console.error('Failed to fetch UGVs:', error);
      }
    };
    fetchUGVs();
  }, []);

  const handleAction = async (action, ugvID) => {
    try {
      await axios.post(`${API_URL}/${action}`, { ugvID });
      toast.success('Request successful');
    } catch (error) {
      toast.error('Failed to send request');
      console.error(`Failed to ${action}:`, error);
    }
  };

  return (
    <div className="p-4 space-y-4">
      <Toaster />
      {ugvs.map((ugv) => (
        <Card key={ugv.id}>
          <CardContent>
            <h2 className="text-xl font-bold">{ugv.name}</h2>
            <p>Type: {ugv.type}</p>
            <p>Status: {ugv.status}</p>
            <p>Session ID: {ugv.sessionId}</p>
          </CardContent>
          <div className="space-x-2">
            <Button onClick={() => handleAction('park', ugv.id)} variant="outline">Park</Button>
            <Button onClick={() => handleAction('retrieve', ugv.id)} variant="default">Retrieve</Button>
          </div>
        </Card>
      ))}
    </div>
  );
}
