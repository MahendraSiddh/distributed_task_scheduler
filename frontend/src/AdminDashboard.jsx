import React, { useState, useEffect, useRef } from 'react';
import { Play, CheckCircle, Clock, XCircle, RefreshCw, LogOut, Users, Plus, AlertTriangle } from 'lucide-react';

const API_BASE_URL = 'http://localhost:8080/api';

const AdminDashboard = ({ user, onLogout }) => {
  const [tasks, setTasks] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [stats, setStats] = useState({ pending: 0, running: 0, completed: 0, failed: 0 });
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newTask, setNewTask] = useState({ name: '', description: '', priority: 3 });

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 3000);
    return () => clearInterval(interval);
  }, []);

  const fetchData = async () => {
    try {
      await Promise.all([
        fetchTasks(),
        fetchEmployees(),
        fetchStatistics()
      ]);
    } catch (error) {
      console.error('Error fetching data:', error);
    }
  };

  const fetchTasks = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/tasks`, {
        headers: { 'User-Id': user.id }
      });
      if (response.ok) {
        const data = await response.json();
        setTasks(data);
      }
    } catch (error) {
      console.error('Error fetching tasks:', error);
    }
  };

  const fetchEmployees = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/employees`, {
        headers: { 'User-Id': user.id }
      });
      if (response.ok) {
        const data = await response.json();
        setEmployees(data);
      }
    } catch (error) {
      console.error('Error fetching employees:', error);
    }
  };

  const fetchStatistics = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/admin/tasks/statistics`, {
        headers: { 'User-Id': user.id }
      });
      if (response.ok) {
        const data = await response.json();
        setStats(data);
      }
    } catch (error) {
      console.error('Error fetching statistics:', error);
    }
  };

  const createTask = async () => {
    if (!newTask.name || !newTask.description) {
      alert('Please fill in all fields');
      return;
    }

    try {
      const response = await fetch(`${API_BASE_URL}/admin/tasks`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'User-Id': user.id
        },
        body: JSON.stringify(newTask)
      });

      if (response.ok) {
        setShowCreateModal(false);
        setNewTask({ name: '', description: '', priority: 3 });
        fetchData();
      } else {
        const error = await response.json();
        alert(error.error || 'Failed to create task');
      }
    } catch (error) {
      console.error('Error creating task:', error);
      alert('Failed to create task');
    }
  };

  const getStatusIcon = (status) => {
    switch(status) {
      case 'PENDING': return <Clock className="w-4 h-4 text-yellow-500" />;
      case 'RUNNING': return <RefreshCw className="w-4 h-4 text-blue-500 animate-spin" />;
      case 'COMPLETED': return <CheckCircle className="w-4 h-4 text-green-500" />;
      case 'FAILED': return <XCircle className="w-4 h-4 text-red-500" />;
      default: return null;
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

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-900 via-blue-900 to-gray-900 text-white p-6">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="mb-8 flex items-center justify-between">
          <div>
            <h1 className="text-4xl font-bold mb-2 bg-gradient-to-r from-blue-400 to-cyan-400 bg-clip-text text-transparent">
              Admin Dashboard
            </h1>
            <p className="text-gray-400">Welcome back, {user.fullName}</p>
          </div>
          <button
            onClick={onLogout}
            className="flex items-center gap-2 px-4 py-2 bg-red-500/20 text-red-400 rounded-lg hover:bg-red-500/30 transition-all"
          >
            <LogOut className="w-4 h-4" />
            Logout
          </button>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-4 gap-4 mb-6">
          <div className="bg-gray-800/50 backdrop-blur rounded-lg p-4 border border-yellow-500/30">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-yellow-400 text-sm font-medium">Pending</p>
                <p className="text-3xl font-bold">{stats.pending || 0}</p>
              </div>
              <Clock className="w-8 h-8 text-yellow-500" />
            </div>
          </div>
          
          <div className="bg-gray-800/50 backdrop-blur rounded-lg p-4 border border-blue-500/30">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-blue-400 text-sm font-medium">Running</p>
                <p className="text-3xl font-bold">{stats.running || 0}</p>
              </div>
              <RefreshCw className="w-8 h-8 text-blue-500" />
            </div>
          </div>
          
          <div className="bg-gray-800/50 backdrop-blur rounded-lg p-4 border border-green-500/30">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-green-400 text-sm font-medium">Completed</p>
                <p className="text-3xl font-bold">{stats.completed || 0}</p>
              </div>
              <CheckCircle className="w-8 h-8 text-green-500" />
            </div>
          </div>
          
          <div className="bg-gray-800/50 backdrop-blur rounded-lg p-4 border border-red-500/30">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-red-400 text-sm font-medium">Failed</p>
                <p className="text-3xl font-bold">{stats.failed || 0}</p>
              </div>
              <XCircle className="w-8 h-8 text-red-500" />
            </div>
          </div>
        </div>

        {/* Control Panel */}
        <div className="bg-gray-800/50 backdrop-blur rounded-lg p-4 mb-6 border border-gray-700">
          <button
            onClick={() => setShowCreateModal(true)}
            className="bg-gradient-to-r from-blue-500 to-cyan-500 hover:from-blue-600 hover:to-cyan-600 px-6 py-2 rounded-lg font-medium transition-all transform hover:scale-105"
          >
            <Plus className="w-4 h-4 inline mr-2" />
            Create New Task
          </button>
          <span className="ml-4 text-sm text-gray-400">
            Tasks are automatically assigned to employees based on priority and fair distribution
          </span>
        </div>

        <div className="grid grid-cols-2 gap-6 mb-6">
          {/* Employees */}
          <div className="bg-gray-800/50 backdrop-blur rounded-lg p-6 border border-gray-700">
            <h2 className="text-xl font-bold mb-4 flex items-center gap-2">
              <Users className="w-5 h-5 text-blue-400" />
              Employees ({employees.length})
            </h2>
            <div className="space-y-3 max-h-96 overflow-y-auto">
              {employees.length === 0 ? (
                <div className="text-center text-gray-500 py-4">No employees registered</div>
              ) : (
                employees.map(employee => (
                  <div key={employee.id} className="bg-gray-900/50 rounded-lg p-3 border border-gray-600">
                    <div className="flex items-center justify-between mb-2">
                      <div>
                        <span className="font-medium">{employee.fullName}</span>
                        <span className="text-xs text-gray-500 ml-2">@{employee.username}</span>
                      </div>
                      <span className={`px-2 py-1 rounded text-xs font-medium ${
                        employee.stats.isWorking ? 'bg-blue-500/20 text-blue-400' : 'bg-green-500/20 text-green-400'
                      }`}>
                        {employee.stats.isWorking ? 'Working' : 'Idle'}
                      </span>
                    </div>
                    <div className="text-sm text-gray-400 grid grid-cols-3 gap-2">
                      <div>Assigned: {employee.stats.totalAssigned}</div>
                      <div>Completed: {employee.stats.totalCompleted}</div>
                      <div>Failed: {employee.stats.totalFailed}</div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Priority Queue */}
          <div className="bg-gray-800/50 backdrop-blur rounded-lg p-6 border border-gray-700">
            <h2 className="text-xl font-bold mb-4">Task Priority Queue</h2>
            <div className="space-y-2 max-h-96 overflow-y-auto">
              {tasks
                .filter(t => t.status === 'PENDING')
                .sort((a, b) => a.priority - b.priority)
                .slice(0, 10)
                .map(task => (
                  <div key={task.taskId} className="bg-gray-900/50 rounded-lg p-3 border border-gray-600">
                    <div className="flex items-center justify-between mb-2">
                      <span className="font-medium text-sm">{task.name}</span>
                      {getPriorityBadge(task.priority)}
                    </div>
                    <p className="text-xs text-gray-400 truncate">{task.description}</p>
                  </div>
                ))
              }
              {tasks.filter(t => t.status === 'PENDING').length === 0 && (
                <div className="text-center text-gray-500 py-8">No pending tasks</div>
              )}
            </div>
          </div>
        </div>

        {/* All Tasks */}
        <div className="bg-gray-800/50 backdrop-blur rounded-lg p-6 border border-gray-700">
          <h2 className="text-xl font-bold mb-4">All Tasks</h2>
          <div className="space-y-2">
            {tasks.length === 0 ? (
              <div className="text-center text-gray-500 py-8">No tasks yet</div>
            ) : (
              tasks.slice(0, 15).map(task => (
                <div key={task.taskId} className="bg-gray-900/50 rounded-lg p-4 border border-gray-600">
                  <div className="flex items-start justify-between mb-2">
                    <div className="flex items-center gap-3 flex-1">
                      {getStatusIcon(task.status)}
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-1">
                          <span className="font-medium">{task.name}</span>
                          {getPriorityBadge(task.priority)}
                          <span className="text-xs text-gray-500">{task.taskId}</span>
                        </div>
                        <p className="text-sm text-gray-400">{task.description}</p>
                      </div>
                    </div>
                    {task.assignedTo && (
                      <span className="text-xs text-blue-400 ml-2">
                        Assigned to: {task.assignedTo.fullName || task.assignedTo.username}
                      </span>
                    )}
                  </div>
                  {task.status === 'RUNNING' && (
                    <div className="w-full bg-gray-700 rounded-full h-2 overflow-hidden mt-2">
                      <div 
                        className="bg-gradient-to-r from-blue-500 to-cyan-500 h-full transition-all duration-300"
                        style={{ width: `${task.progress || 0}%` }}
                      />
                    </div>
                  )}
                  {task.completionMessage && (
                    <div className="text-xs text-green-400 mt-2 bg-green-500/10 p-2 rounded">
                      ✓ {task.completionMessage}
                    </div>
                  )}
                  {task.errorMessage && (
                    <div className="text-xs text-red-400 mt-2 bg-red-500/10 p-2 rounded">
                      ✗ {task.errorMessage}
                    </div>
                  )}
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      {/* Create Task Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-gray-800 rounded-lg p-6 w-full max-w-md border border-gray-700">
            <h3 className="text-xl font-bold mb-4">Create New Task</h3>
            
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium mb-2">Task Name</label>
                <input
                  type="text"
                  value={newTask.name}
                  onChange={(e) => setNewTask({ ...newTask, name: e.target.value })}
                  className="w-full px-3 py-2 bg-gray-900 border border-gray-600 rounded-lg focus:outline-none focus:border-blue-500"
                  placeholder="Enter task name"
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-2">Description</label>
                <textarea
                  value={newTask.description}
                  onChange={(e) => setNewTask({ ...newTask, description: e.target.value })}
                  className="w-full px-3 py-2 bg-gray-900 border border-gray-600 rounded-lg focus:outline-none focus:border-blue-500 h-24"
                  placeholder="Enter task description"
                />
              </div>

              <div>
                <label className="block text-sm font-medium mb-2">
                  Priority: {newTask.priority} - {
                    {1: 'Critical', 2: 'High', 3: 'Medium', 4: 'Low', 5: 'Very Low'}[newTask.priority]
                  }
                </label>
                <input
                  type="range"
                  min="1"
                  max="5"
                  value={newTask.priority}
                  onChange={(e) => setNewTask({ ...newTask, priority: parseInt(e.target.value) })}
                  className="w-full"
                />
                <div className="flex justify-between text-xs text-gray-500 mt-1">
                  <span>Critical</span>
                  <span>Very Low</span>
                </div>
              </div>
            </div>

            <div className="flex gap-3 mt-6">
              <button
                onClick={createTask}
                className="flex-1 bg-gradient-to-r from-blue-500 to-cyan-500 px-4 py-2 rounded-lg font-medium hover:from-blue-600 hover:to-cyan-600"
              >
                Create Task
              </button>
              <button
                onClick={() => setShowCreateModal(false)}
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

export default AdminDashboard;