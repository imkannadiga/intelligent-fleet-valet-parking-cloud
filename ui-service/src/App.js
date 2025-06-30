import logo from './logo.svg';
import './App.css';
import ValetParking from './componants/ValetParking';
import SimpleNaviggator from './componants/SimpleNavigator';
import MultiMapViewer from './componants/MultiMapViewer';

function App() {
  return (
    <div>
      <h1>Valet Parking</h1>
      <ValetParking />
      <MultiMapViewer />
    </div>
  );
}

export default App;
