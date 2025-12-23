import React, { useState, useEffect, useRef } from 'react';
import { CheckCircle, XCircle, Clock, AlertCircle, LogOut, Play, TrendingUp, Award, Wifi, WifiOff, MessageSquare } from 'lucide-react';

const API_BASE_URL = 'http://localhost:8080/api';
const WS_URL = 'ws://localhost:8080/ws';

const EmployeeDashboard = ({ user, onLogout }) => {
  const [currentTask, setCurrentTask] = useState(null);
  const [stats, setStats] = useState({
    totalAssigned: 0,
    totalCompleted: 0,
    totalFailed: 0,
    isWorking: false,
    averageTimeMinutes: 0
  });
  const [recentTasks, setRecentTasks] = useState([]);
  const [comment, setComment] = useState('');
  const [showCompleteModal, setShowCompleteModal] = useState(false);
  const [showFailModal, setShowFailModal] = useState(false);
  const [progress, setProgress] = useState(0);
  const [wsConnected, setWsConnected] = useState(false);
  const [notification, setNotification] = useState(null);
  const wsRef = useRef(null);

  useEffect(() => {
    fetchDashboard();
    connectWebSocket();
    
    const interval = setInterval(fetchDashboard, 10000); // Fallback polling
    
    return () => {
      clearInterval(interval);
      if (wsRef.current) {
        wsRef.current.close();
      }
    };
  }, []);

  useEffect(() => {
    if (currentTask && currentTask.status === 'RUNNING') {
      setProgress(currentTask.progress || 0);
    }
  }, [currentTask]);

  const connectWebSocket = () => {
    try {
      const ws = new WebSocket(WS_URL);
      
      ws.onopen = () => {
        console.log('Employee WebSocket connected');
        setWsConnected(true);
        
        // Subscribe to user-specific queue
        const subscribeMessage = JSON.stringify({
          action: 'subscribe',
          userId: user.id,
          topics: [`/user/${user.id}/queue/tasks`]
        });
        ws.send(subscribeMessage);
      };

      ws.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data);
          handleWebSocketMessage(message);
        } catch (error) {
          console.error('Error parsing WebSocket message:', error);
        }
      };

      ws.onerror = (error) => {
        console.error('WebSocket error:', error);
        setWsConnected(false);
      };

      ws.onclose = () => {
        console.log('WebSocket disconnected');
        setWsConnected(false);
        
        // Reconnect after 3 seconds
        setTimeout(connectWebSocket, 3000);
      };

      wsRef.current = ws;
    } catch (error) {
      console.error('Failed to connect WebSocket:', error);
      setWsConnected(false);
    }
  };

  const handleWebSocketMessage = (message) => {
    console.log('WebSocket message received:', message);
    
    switch(message.type) {
      case 'TASK_ASSIGNED':
        const assignment = message.payload;
        showNotification(`🎯 New task assigned: ${assignment.taskName}`, 'info');
        fetchDashboard();
        break;
        
      case 'TASK_COMPLETED':
        showNotification(`✓ Task completed successfully!`, 'success');
        fetchDashboard();
        break;
        
      case 'TASK_FAILED':
        showNotification(`Task marked as failed`, 'error');
        fetchDashboard();
        break;
        
      default:
        console.log('Unknown message type:', message.type);
    }
  };

  const showNotification = (text, type) => {
    setNotification({ text, type, timestamp: new Date().toLocaleTimeString() });
    
    // Auto-hide after 5 seconds
    setTimeout(() => {
      setNotification(null);
    }, 5000);
  };

  const fetchDashboard = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/employee/tasks/dashboard`, {
        headers: { 'User-Id': user.id }
      });
      
      if (response.ok) {
        const data = await response.json();
        setCurrentTask(data.currentTask);
        setStats(data.stats);
        setRecentTasks(data.recentTasks || []);
      }
    } catch (error) {
      console.error('Error fetching dashboard:', error);
    }
  };

  const getNextTask = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/employee/tasks/get-next`, {
        method: 'POST',
        headers: { 'User-Id': user.id }
      });
      
      if (response.ok) {
        const data = await response.json();
        if (data.message) {
          showNotification(data.message, 'info');
        } else {
          setCurrentTask(data);
          showNotification('New task received!', 'success');
          fetchDashboard();
        }
      }
    } catch (error) {
      console.error('Error getting next task:', error);
    }
  };

  const updateProgress = async (newProgress) => {
    if (!currentTask) return;
    
    setProgress(newProgress);
    
    try {
      await fetch(`${API_BASE_URL}/employee/tasks/${currentTask.taskId}/progress`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'User-Id': user.id
        },
        body: JSON.stringify({ progress: newProgress })
      });
    } catch (error) {
      console.error('Error updating progress:', error);
    }
  };

  const handleCompleteTask = async () => {
    if (!currentTask) return;
    
    if (!comment || comment.trim().length === 0) {
      alert('Please provide completion comments before submitting');
      return;
    }
    
    try {
      const response = await fetch(`${API_BASE_URL}/employee/tasks/${currentTask.taskId}/complete`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'User-Id': user.id
        },
        body: JSON.stringify({ message: comment })
      });
      
      if (response.ok) {
        setShowCompleteModal(false);
        setComment('');
        setProgress(0);
        showNotification('Task completed successfully!', 'success');
        fetchDashboard();
      } else {
        const error = await response.json();
        alert(error.error || 'Failed to complete task');
      }
    } catch (error) {
      console.error('Error completing task:', error);
      alert('Failed to complete task');
    }
  };

  const handleFailTask = async () => {
    if (!currentTask) return;
    
    if (!comment || comment.trim().length === 0) {
      alert('Please provide failure reason before submitting');
      return;
    }
    
    try {
      const response = await fetch(`${API_BASE_URL}/employee/tasks/${currentTask.taskId}/fail`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'User-Id': user.id
        },
        body: JSON.stringify({ message: comment })
      });
      
      if (response.ok) {
        setShowFailModal(false);
        setComment('');
        setProgress(0);
        showNotification('Task marked as failed', 'error');
        fetchDashboard();
      } else {
        const error = await response.json();
        alert(error.error || 'Failed to update task');
      }
    } catch (error) {
      console.error('Error failing task:', error);
      alert('Failed to update task');
    }
  };

  const getPriorityBadge = (priority) => {
    const colors = {
      1: 'bg-red-500/20 text-red-400 border-red-500',
      2: 'bg-orange-500/20 text-orange-400 border-orange-500',
      3: 'bg-yellow-500/20 text-yellow-400 border-yellow-500',
      4: 'bg-blue-500/20 text-blue-400 border-blue-500',
      5: 'bg-gray-500/20 text-gray-400 border-gray-500'
    };
    const labels = { 1: 'Critical', 2: 'High', 3: 'Medium', 4: 'Low', 5: 'Very Low' };
    
    return (
      <span className={`px-2 py-1 rounded text-xs font-medium border ${colors[priority]}`}>
        P{priority} - {labels[priority]}
      </span>
    );
  };

  const getStatusIcon = (status) => {
    switch(status) {
      case 'COMPLETED': return <CheckCircle className="w-4 h-4 text-green-500" />;
      case 'FAILED': return <XCircle className="w-4 h-4 text-red-500" />;
      case 'RUNNING': return <Clock className="w-4 h-4 text-blue-500" />;
      default: return <Clock className="w-4 h-4 text-gray-500" />;
    }
  };

  const completionRate = stats.totalAssigned > 0 
    ? ((stats.totalCompleted / stats.totalAssigned) * 100).toFixed(1)
    : 0;

  const getNotificationColor = (type) => {
    switch(type) {
      case 'success': return 'bg-green-500/20 border-green-500 text-green-400';
      case 'error': return 'bg-red-500/20 border-red-500 text-red-400';
      default: return 'bg-blue-500/20 border-blue-500 text-blue-400';
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-900 via-purple-900 to-gray-900 text-white p-6">
      <div className="max-w-6xl mx-auto">
        {/* Header */}
        <div className="mb-8 flex items-center justify-between">
          <div>
            <h1 className="text-4xl font-bold mb-2 bg-gradient-to-r from-purple-400 to-pink-400 bg-clip-text text-transparent">
              Employee Dashboard
            </h1>
            <p className="text-gray-400">Welcome, {user.fullName}</p>
          </div>
          <div className="flex items-center gap-3">
            <div className={`flex items-center gap-2 px-3 py-1 rounded-full text-sm ${
              wsConnected ? 'bg-green-500/20 text-green-400' : 'bg-red-500/20 text-red-400'
            }`}>
              {wsConnected ? <Wifi className="w-4 h-4" /> : <WifiOff className="w-4 h-4" />}
              {wsConnected ? 'Live' : 'Offline'}
            </div>
            <button
              onClick={onLogout}
              className="flex items-center gap-2 px-4 py-2 bg-red-500/20 text-red-400 rounded-lg hover:bg-red-500/30 transition-all"
            >
              <LogOut className="w-4 h-4" />
              Logout
            </button>
          </div>
        </div>

        {/* Real-time Notification */}
        {notification && (
          <div className="fixed top-6 right-6 z-50">
            <div className={`px-4 py-3 rounded-lg border backdrop-blur animate-pulse ${getNotificationColor(notification.type)}`}>
              <div className="flex items-center gap-2">
                <span className="text-sm font-medium">{notification.text}</span>
                <span className="text-xs opacity-70">{notification.timestamp}</span>
              </div>
            </div>
          </div>
        )}

        {/* Stats Cards */}
        <div className="grid grid-cols-4 gap-4 mb-6">
          <div className="bg-gray-800/50 backdrop-blur rounded-lg p-4 border border-blue-500/30">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-blue-400 text-sm font-medium">Assigned</p>
                <p className="text-3xl font-bold">{stats.totalAssigned}</p>
              </div>
              <Clock className="w-8 h-8 text-blue-500" />
            </div>
          </div>
          
          <div className="bg-gray-800/50 backdrop-blur rounded-lg p-4 border border-green-500/30">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-green-400 text-sm font-medium">Completed</p>
                <p className="text-3xl font-bold">{stats.totalCompleted}</p>
              </div>
              <CheckCircle className="w-8 h-8 text-green-500" />
            </div>
          </div>
          
          <div className="bg-gray-800/50 backdrop-blur rounded-lg p-4 border border-red-500/30">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-red-400 text-sm font-medium">Failed</p>
                <p className="text-3xl font-bold">{stats.totalFailed}</p>
              </div>
              <XCircle className="w-8 h-8 text-red-500" />
            </div>
          </div>
          
          <div className="bg-gray-800/50 backdrop-blur rounded-lg p-4 border border-purple-500/30">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-purple-400 text-sm font-medium">Success Rate</p>
                <p className="text-3xl font-bold">{completionRate}%</p>
              </div>
              <Award className="w-8 h-8 text-purple-500" />
            </div>
          </div>
        </div>

        {/* Current Task */}
        <div className="bg-gray-800/50 backdrop-blur rounded-lg p-6 border border-gray-700 mb-6">
          <h2 className="text-2xl font-bold mb-4 flex items-center gap-2">
            <Play className="w-6 h-6 text-blue-400" />
            Current Task
            {wsConnected && <span className="text-xs text-green-400 ml-2">(Real-time updates active)</span>}
          </h2>
          
          {currentTask ? (
            <div className="space-y-4">
              <div className="bg-gray-900/50 rounded-lg p-4 border border-blue-500/30">
                <div className="flex items-start justify-between mb-3">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      <h3 className="text-xl font-bold">{currentTask.name}</h3>
                      {getPriorityBadge(currentTask.priority)}
                    </div>
                    <p className="text-gray-300 mb-2">{currentTask.description}</p>
                    <p className="text-xs text-gray-500">Task ID: {currentTask.taskId}</p>
                  </div>
                </div>

                {/* Progress Bar */}
                <div className="mb-4">
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-sm text-gray-400">Progress</span>
                    <span className="text-sm font-bold text-blue-400">{progress}%</span>
                  </div>
                  <div className="w-full bg-gray-700 rounded-full h-3 overflow-hidden">
                    <div 
                      className="bg-gradient-to-r from-blue-500 to-purple-500 h-full transition-all duration-300"
                      style={{ width: `${progress}%` }}
                    />
                  </div>
                  
                  {/* Progress Slider */}
                  <input
                    type="range"
                    min="0"
                    max="100"
                    value={progress}
                    onChange={(e) => updateProgress(parseInt(e.target.value))}
                    className="w-full mt-3"
                  />
                </div>

                {/* Action Buttons */}
                <div className="flex gap-3">
                  <button
                    onClick={() => {
                      setComment('');
                      setShowCompleteModal(true);
                    }}
                    className="flex-1 bg-gradient-to-r from-green-500 to-emerald-500 px-4 py-3 rounded-lg font-medium hover:from-green-600 hover:to-emerald-600 transition-all flex items-center justify-center gap-2"
                  >
                    <CheckCircle className="w-4 h-4" />
                    Complete Task
                  </button>
                  <button
                    onClick={() => {
                      setComment('');
                      setShowFailModal(true);
                    }}
                    className="flex-1 bg-gradient-to-r from-red-500 to-pink-500 px-4 py-3 rounded-lg font-medium hover:from-red-600 hover:to-pink-600 transition-all flex items-center justify-center gap-2"
                  >
                    <XCircle className="w-4 h-4" />
                    Report Failure
                  </button>
                </div>
              </div>
            </div>
          ) : (
            <div className="text-center py-12">
              <AlertCircle className="w-16 h-16 text-gray-600 mx-auto mb-4" />
              <p className="text-gray-400 mb-4">No active task assigned</p>
              <button
                onClick={getNextTask}
                className="bg-gradient-to-r from-blue-500 to-purple-500 px-6 py-3 rounded-lg font-medium hover:from-blue-600 hover:to-purple-600 transition-all"
              >
                <Play className="w-4 h-4 inline mr-2" />
                Get Next Task
              </button>
            </div>
          )}
        </div>

        {/* Task History */}
        <div className="bg-gray-800/50 backdrop-blur rounded-lg p-6 border border-gray-700">
          <h2 className="text-xl font-bold mb-4 flex items-center gap-2">
            <TrendingUp className="w-5 h-5 text-purple-400" />
            Task History
          </h2>
          <div className="space-y-2 max-h-96 overflow-y-auto">
            {recentTasks.length === 0 ? (
              <div className="text-center text-gray-500 py-8">No task history yet</div>
            ) : (
              recentTasks.slice(0, 10).map(task => (
                <div key={task.taskId} className="bg-gray-900/50 rounded-lg p-3 border border-gray-600">
                  <div className="flex items-start justify-between mb-2">
                    <div className="flex items-center gap-3 flex-1">
                      {getStatusIcon(task.status)}
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-1">
                          <span className="font-medium">{task.name}</span>
                          {getPriorityBadge(task.priority)}
                        </div>
                        <p className="text-xs text-gray-400">{task.description}</p>
                      </div>
                    </div>
                    <span className={`text-xs px-2 py-1 rounded ${
                      task.status === 'COMPLETED' ? 'bg-green-500/20 text-green-400' :
                      task.status === 'FAILED' ? 'bg-red-500/20 text-red-400' :
                      'bg-blue-500/20 text-blue-400'
                    }`}>
                      {task.status}
                    </span>
                  </div>
                  {task.completionMessage && (
                    <div className="text-xs text-green-400 mt-2 bg-green-500/10 p-2 rounded flex items-start gap-2">
                      <MessageSquare className="w-3 h-3 mt-0.5 flex-shrink-0" />
                      <span>{task.completionMessage}</span>
                    </div>
                  )}
                  {task.errorMessage && (
                    <div className="text-xs text-red-400 mt-2 bg-red-500/10 p-2 rounded flex items-start gap-2">
                      <MessageSquare className="w-3 h-3 mt-0.5 flex-shrink-0" />
                      <span>{task.errorMessage}</span>
                    </div>
                  )}
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      {/* Complete Task Modal */}
      {showCompleteModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-gray-800 rounded-lg p-6 w-full max-w-md border border-gray-700">
            <h3 className="text-xl font-bold mb-4 text-green-400">Complete Task</h3>
            
            <div className="mb-4">
              <label className="block text-sm font-medium mb-2">
                Completion Comments <span className="text-red-400">*</span>
              </label>
              <textarea
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                className="w-full px-3 py-2 bg-gray-900 border border-gray-600 rounded-lg focus:outline-none focus:border-green-500 h-32"
                placeholder="Describe what you accomplished, any issues resolved, or important notes..."
                required
              />
              <p className="text-xs text-gray-500 mt-1">
                Required: Please provide details about task completion
              </p>
            </div>

            <div className="flex gap-3">
              <button
                onClick={handleCompleteTask}
                disabled={!comment || comment.trim().length === 0}
                className={`flex-1 px-4 py-2 rounded-lg font-medium transition-all ${
                  comment && comment.trim().length > 0
                    ? 'bg-gradient-to-r from-green-500 to-emerald-500 hover:from-green-600 hover:to-emerald-600'
                    : 'bg-gray-600 cursor-not-allowed'
                }`}
              >
                Confirm Completion
              </button>
              <button
                onClick={() => {
                  setShowCompleteModal(false);
                  setComment('');
                }}
                className="flex-1 bg-gray-700 px-4 py-2 rounded-lg font-medium hover:bg-gray-600"
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Fail Task Modal */}
      {showFailModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-gray-800 rounded-lg p-6 w-full max-w-md border border-gray-700">
            <h3 className="text-xl font-bold mb-4 text-red-400">Report Task Failure</h3>
            
            <div className="mb-4">
              <label className="block text-sm font-medium mb-2">
                Failure Reason <span className="text-red-400">*</span>
              </label>
              <textarea
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                className="w-full px-3 py-2 bg-gray-900 border border-gray-600 rounded-lg focus:outline-none focus:border-red-500 h-32"
                placeholder="Explain what went wrong, any blockers encountered, or technical issues..."
                required
              />
              <p className="text-xs text-gray-500 mt-1">
                Required: Please explain why the task could not be completed
              </p>
            </div>

            <div className="flex gap-3">
              <button
                onClick={handleFailTask}
                disabled={!comment || comment.trim().length === 0}
                className={`flex-1 px-4 py-2 rounded-lg font-medium transition-all ${
                  comment && comment.trim().length > 0
                    ? 'bg-gradient-to-r from-red-500 to-pink-500 hover:from-red-600 hover:to-pink-600'
                    : 'bg-gray-600 cursor-not-allowed'
                }`}
              >
                Confirm Failure
              </button>
              <button
                onClick={() => {
                  setShowFailModal(false);
                  setComment('');
                }}
                className="flex-1 bg-gray-700 px-4 py-2 rounded-lg font-medium hover:bg-gray-600"
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default EmployeeDashboard;